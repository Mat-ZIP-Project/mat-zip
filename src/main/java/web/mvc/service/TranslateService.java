/*package web.mvc.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final Translate translate;

    public String translateText(String text, String targetLanguage) {
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.model("nmt") // neural machine translation
        );
        return translation.getTranslatedText();
    }
}*/
