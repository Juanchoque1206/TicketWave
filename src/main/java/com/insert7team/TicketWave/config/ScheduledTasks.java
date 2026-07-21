package com.insert7team.TicketWave.config;

import com.insert7team.TicketWave.notification.service.NotificationService;
import com.insert7team.TicketWave.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final OrderService orderService;
    private final NotificationService notificationService;

    public ScheduledTasks(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void expireStaleOrders() {
        log.debug("Running stale order expiration check...");
        orderService.expireStaleOrders();
    }

    @Scheduled(fixedRate = 300000)
    public void retryFailedNotifications() {
        log.debug("Running failed notification retry...");
        notificationService.retryFailedNotifications();
    }
}
