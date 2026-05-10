package com.williamfds.integrationhub.application.usecase;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class RegisterIncomingOrder {

    private final OrderRepository orders;

    public RegisterIncomingOrder(OrderRepository orders) {
        this.orders = orders;
    }

    public Order handle(Order canonical) {
        // TODO (Semana 2): idempotência real via X-Webhook-Id + Redis.
        // Por enquanto, atualizamos o status se o pedido já existe e
        // tratamos qualquer outra mudança como insert novo.
        return orders.findByPlatformAndExternalId(canonical.platform(), canonical.externalId())
                .map(existing -> orders.save(existing.withStatus(canonical.status())))
                .orElseGet(() -> orders.save(canonical));
    }
}
