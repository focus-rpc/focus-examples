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

import java.io.IOException;

import com.dinstone.focus.TelemetryHelper;
import com.dinstone.focus.example.UserCheckService;
import com.dinstone.focus.example.UserCheckServiceImpl;
import com.dinstone.focus.invoke.Interceptor;
import com.dinstone.focus.serialize.protobuf.ProtobufSerializer;
import com.dinstone.focus.telemetry.TelemetryInterceptor;
import com.dinstone.focus.transport.photon.PhotonAcceptOptions;
import com.dinstone.loghub.Logger;
import com.dinstone.loghub.LoggerFactory;
import io.opentelemetry.api.OpenTelemetry;

public class UserServiceServer {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceServer.class);

    public static void main(String[] args) {

        FocusServer uss = createUserServiceServer();

        LOG.info("server start");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        uss.close();
        LOG.info("server stop");
    }

    private static FocusServer createUserServiceServer() {

        String serviceName = "user.service";
        OpenTelemetry openTelemetry = TelemetryHelper.getTelemetry(serviceName);
        Interceptor tf = new TelemetryInterceptor(openTelemetry, Interceptor.Kind.SERVER);

        ServerOptions serverOptions = new ServerOptions(serviceName);
        serverOptions.listen("localhost", 3301);
        serverOptions.addInterceptor(tf).setAcceptOptions(new PhotonAcceptOptions());
        FocusServer server = new FocusServer(serverOptions);
        server.exporting(UserCheckService.class, new UserCheckServiceImpl(),
                new ExportOptions(UserCheckService.class.getName())
                        .setSerializerType(ProtobufSerializer.SERIALIZER_TYPE));

        server.start();
        return server;
    }

}
