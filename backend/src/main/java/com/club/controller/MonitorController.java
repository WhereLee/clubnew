package com.club.controller;

import com.club.common.R;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 监控中心：运行概览。
 * <p>
 * 数据源为应用内存中的 Micrometer MeterRegistry（本机零外部依赖）：
 * Prometheus 是另一条平行采集通道（/actuator/prometheus），互不影响。
 * 概览页数字与 Grafana 同源同值。
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private final MeterRegistry meterRegistry;

    public MonitorController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PreAuthorize("@ss.hasPermi('monitor:overview:list')")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jvm", jvmMetrics());
        data.put("http", httpMetrics());
        data.put("business", businessCounters());
        data.put("uptimeSec", ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0);
        return R.success(data);
    }

    /** JVM：堆内存 / 存活线程 / GC */
    private Map<String, Object> jvmMetrics() {
        double heapUsed = sumGauges("jvm.memory.used", "area", "heap");
        double heapMax = sumGauges("jvm.memory.max", "area", "heap");
        double gcCount = countOfTimers("jvm.gc.pause");
        double gcTimeSec = sumTimers("jvm.gc.pause", TimeUnit.SECONDS);

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("heapUsedMb", Math.round(heapUsed / 1024 / 1024 * 10) / 10.0);
        jvm.put("heapMaxMb", Math.round(heapMax / 1024 / 1024 * 10) / 10.0);
        jvm.put("heapUsagePct", heapMax > 0 ? Math.round(heapUsed / heapMax * 1000) / 10.0 : 0.0);
        jvm.put("threadsLive", (long) lastGauge("jvm.threads.live"));
        jvm.put("gcCount", (long) gcCount);
        jvm.put("gcTimeSec", Math.round(gcTimeSec * 100) / 100.0);
        return jvm;
    }

    /** HTTP：请求总量 / 平均耗时 / 最大耗时（P95 等分位留给 Grafana histogram_quantile） */
    private Map<String, Object> httpMetrics() {
        double count = countOfTimers("http.server.requests");
        double totalMs = sumTimers("http.server.requests", TimeUnit.MILLISECONDS);
        double maxMs = maxTimers("http.server.requests", TimeUnit.MILLISECONDS);

        Map<String, Object> http = new LinkedHashMap<>();
        http.put("requestsTotal", (long) count);
        http.put("avgMs", count > 0 ? Math.round(totalMs / count * 10) / 10.0 : 0.0);
        http.put("maxMs", Math.round(maxMs * 10) / 10.0);
        return http;
    }

    /** 业务计数器一览（命名规范 club_<业务>_<动作>_total） */
    private List<Map<String, Object>> businessCounters() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] names = {
                "club_recruit_apply_total", "club_activity_signup_total",
                "club_stock_prededuct_failures_total", "club_ratelimit_rejections_total",
                "club_repeatsubmit_rejections_total", "club_stream_event_published_total",
                "club_stream_event_publish_fallbacks_total", "club_stream_event_consumed_total",
                "club_stream_event_consume_failures_total", "club_audit_approved_total",
                "club_audit_rejected_total", "club_agent_tool_success_total",
                "club_agent_tool_failure_total", "club_agent_tool_denied_total",
                "club_login_failed_total"
        };
        for (String name : names) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", name);
            item.put("value", (long) sumCounters(name));
            list.add(item);
        }
        return list;
    }

    // ---- MeterRegistry 聚合工具 ----

    private double sumGauges(String name, String tagKey, String tagValue) {
        double sum = 0;
        for (var gauge : meterRegistry.find(name).tag(tagKey, tagValue).gauges()) {
            sum += gauge.value();
        }
        return sum;
    }

    private double lastGauge(String name) {
        double last = 0;
        for (var gauge : meterRegistry.find(name).gauges()) {
            last = gauge.value();
        }
        return last;
    }

    private double sumCounters(String name) {
        double sum = 0;
        for (Counter c : meterRegistry.find(name).counters()) {
            sum += c.count();
        }
        return sum;
    }

    /** Timer 的调用次数之和（http.server.requests / jvm.gc.pause 均为 Timer 类型） */
    private double countOfTimers(String name) {
        double count = 0;
        for (Timer t : meterRegistry.find(name).timers()) {
            count += t.count();
        }
        return count;
    }

    private double sumTimers(String name, TimeUnit unit) {
        double sum = 0;
        for (Timer t : meterRegistry.find(name).timers()) {
            sum += t.totalTime(unit);
        }
        return sum;
    }

    private double maxTimers(String name, TimeUnit unit) {
        double max = 0;
        for (Timer t : meterRegistry.find(name).timers()) {
            max = Math.max(max, t.max(unit));
        }
        return max;
    }
}
