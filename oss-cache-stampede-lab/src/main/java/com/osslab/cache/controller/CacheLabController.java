package com.osslab.cache.controller;

import com.osslab.cache.jitter.NormalKeyJitterService;
import com.osslab.cache.singleflight.HotKeySingleFlightService;
import com.osslab.cache.swr.HotKeySwrService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lab")
public class CacheLabController {

    private final HotKeySingleFlightService singleFlightService;
    private final HotKeySwrService swrService;
    private final NormalKeyJitterService jitterService;

    public CacheLabController(
            HotKeySingleFlightService singleFlightService,
            HotKeySwrService swrService,
            NormalKeyJitterService jitterService) {
        this.singleFlightService = singleFlightService;
        this.swrService = swrService;
        this.jitterService = jitterService;
    }

    @GetMapping("/singleflight/{key}")
    public String getSingleFlightValue(@PathVariable String key) {
        return singleFlightService.getValue(key);
    }

    @GetMapping("/swr/{key}")
    public String getSwrValue(@PathVariable String key) {
        return swrService.getValue(key);
    }

    @GetMapping("/jitter/{key}")
    public String getJitterValue(@PathVariable String key) {
        return jitterService.getValue(key);
    }

    @GetMapping("/clear/all")
    public String clearAllCaches() {
        singleFlightService.clearCache();
        swrService.clearCache();
        jitterService.clearCache();
        return "All caches cleared.";
    }

    @GetMapping("/clear/singleflight")
    public String clearSingleFlightCache() {
        singleFlightService.clearCache();
        return "SingleFlight cache cleared.";
    }

    @GetMapping("/clear/swr")
    public String clearSwrCache() {
        swrService.clearCache();
        return "SWR cache cleared.";
    }

    @GetMapping("/clear/jitter")
    public String clearJitterCache() {
        jitterService.clearCache();
        return "Jitter cache cleared.";
    }
}
