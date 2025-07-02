/*
package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import web.mvc.service.TranslateService;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    @PostMapping
    public String translate(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String targetLang // 기본: 영어
    ) {
        try {
            return translateService.translateText(text, targetLang);
        } catch (Exception e) {
            e.printStackTrace();
            return "번역 실패: " + e.getMessage();
        }
    }
}
*/
