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
package com.dinstone.focus.example;

import com.dinstone.focus.invoke.Context;
import com.dinstone.focus.propagate.Baggage;
import com.dinstone.focus.protobuf.UserCheckRequest;
import com.dinstone.loghub.Logger;
import com.dinstone.loghub.LoggerFactory;

public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private UserCheckService userService;

    private StoreService storeService;

    public OrderServiceImpl(UserCheckService userService, StoreService storeService) {
        super();
        this.userService = userService;
        this.storeService = storeService;
    }

    @Override
    public OrderResponse createOrder(OrderRequest order) {
        UserCheckRequest build = UserCheckRequest.newBuilder().setUserId(order.getUid()).build();
        if (!userService.checkExist(build).getExist()) {
            throw new IllegalArgumentException("user id is valid");
        }
        log.info("user is exist:{}", order.getUid());

        Baggage baggage = Context.current().get(Baggage.CONTEXT_KEY);
        log.info("baggage = {}", baggage);

        storeService.checkExist(order.getUid());
        return new OrderResponse().setOid(order.getUid() + "-" + order.getPoi() + "-" + order.getSn());
    }

    @Override
    public OrderResponse findOldOrder(OrderRequest order) {
        return new OrderResponse().setOid(order.getUid() + "-" + order.getPoi() + "-" + order.getSn());
    }

}
