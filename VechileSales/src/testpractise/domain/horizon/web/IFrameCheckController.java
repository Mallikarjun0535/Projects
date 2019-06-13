package com.dizzion.portal.domain.horizon.web;

import com.dizzion.portal.domain.horizon.IFrameCheckService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class IFrameCheckController {

    private final IFrameCheckService IFrameCheckService;

    public IFrameCheckController(IFrameCheckService IFrameCheckService) {
        this.IFrameCheckService = IFrameCheckService;
    }

    @RequestMapping(path = "/url-embeddable", method = GET)
    public boolean isEmbeddable(@RequestParam String url) {
        return IFrameCheckService.isEmbeddable(url);
    }
}
