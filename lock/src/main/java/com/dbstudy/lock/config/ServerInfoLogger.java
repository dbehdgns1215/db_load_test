package com.dbstudy.lock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerInfoLogger {

    @Value("${app.server-id}")
    private String serverId;

    @Value("${server.port}")
    private String serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void logServerInfo() {
        log.info("========================================");
        log.info("🚀 서버 시작 완료!");
        log.info("📌 서버 ID: {}", serverId);
        log.info("🔌 포트: {}", serverPort);
        log.info("========================================");
    }
}
