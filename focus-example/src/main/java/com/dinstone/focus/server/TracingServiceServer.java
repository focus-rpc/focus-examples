/*
 * Copyright (C) 2019~2023 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.focus.server;

import com.dinstone.focus.TelemetryHelper;
import com.dinstone.focus.client.ClientOptions;
import com.dinstone.focus.client.FocusClient;
import com.dinstone.focus.client.ImportOptions;
import com.dinstone.focus.example.OrderService;
import com.dinstone.focus.example.OrderServiceImpl;
import com.dinstone.focus.example.StoreService;
import com.dinstone.focus.example.UserCheckService;
import com.dinstone.focus.invoke.Interceptor;
import com.dinstone.focus.serialize.protobuf.ProtobufSerializer;
import com.dinstone.focus.telemetry.TelemetryInterceptor;
import com.dinstone.focus.transport.photon.PhotonAcceptOptions;
import com.dinstone.focus.transport.photon.PhotonConnectOptions;
import com.dinstone.loghub.Logger;
import com.dinstone.loghub.LoggerFactory;
import io.opentelemetry.api.OpenTelemetry;

public class TracingServiceServer {

    private static final Logger LOG = LoggerFactory.getLogger(TracingServiceServer.class);

    public static void main(String[] args) {

        try (FocusServer server = createOrderServiceServer()) {
            LOG.info("server start");
            server.start();

            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOG.info("server stop");
    }

    private static FocusServer createOrderServiceServer() {
        String appName = "order.service";
        OpenTelemetry openTelemetry = TelemetryHelper.getTelemetry(appName);
        Interceptor tf = new TelemetryInterceptor(openTelemetry, Interceptor.Kind.SERVER);

        ServerOptions serverOptions = new ServerOptions(appName);
        serverOptions.setAcceptOptions(new PhotonAcceptOptions());
        serverOptions.listen("localhost", 3303);
        serverOptions.addInterceptor(tf);
        FocusServer server = new FocusServer(serverOptions);

        UserCheckService userService = createUserServiceRpc(openTelemetry, appName);
        StoreService storeService = createStoreServiceRpc(openTelemetry, appName);
        OrderService orderService = new OrderServiceImpl(userService, storeService);

        server.exporting(OrderService.class, orderService);

        return server;
    }

    private static StoreService createStoreServiceRpc(OpenTelemetry openTelemetry, String appName) {
        Interceptor tf = new TelemetryInterceptor(openTelemetry, Interceptor.Kind.CLIENT);

        ClientOptions option = new ClientOptions(appName).connect("localhost", 3302)
                .setConnectOptions(new PhotonConnectOptions()).addInterceptor(tf).setConnectRetry(2);
        FocusClient client = new FocusClient(option);
        return client.importing(StoreService.class);
    }

    private static UserCheckService createUserServiceRpc(OpenTelemetry openTelemetry, String appName) {
        Interceptor tf = new TelemetryInterceptor(openTelemetry, Interceptor.Kind.CLIENT);

        ClientOptions option = new ClientOptions(appName).connect("localhost", 3301)
                .setConnectOptions(new PhotonConnectOptions()).addInterceptor(tf).setConnectRetry(2);
        FocusClient client = new FocusClient(option);

        ImportOptions ro = new ImportOptions(UserCheckService.class.getName())
                .setSerializerType(ProtobufSerializer.SERIALIZER_TYPE);
        return client.importing(UserCheckService.class, ro);
    }

}
