package com.ftn.sbnz.service.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        String[] drlPaths = {
                "rules/backward.drl",
                "rules/cep_deduct.drl",
                "rules/cep_strat.drl",
                "rules/forward_deduct.drl",
                "rules/forward_strat.drl"
        };
        for (String path : drlPaths) {
            kfs.write("src/main/resources/" + path,
                    ks.getResources().newClassPathResource(path));
        }

        // NoShow template
        String generatedNoShow = TemplateLoader.generateNoShowRules();
        System.out.println("========== NoShowGenerated.drl ==========");
        System.out.println(generatedNoShow);
        System.out.println("========== END ==========");
        kfs.write("src/main/resources/rules/NoShowGenerated.drl", generatedNoShow);

        // PrivateShow template  <-- nedostajalo
        String generatedPrivateShow = TemplateLoader.generatePrivateShowRules();
        System.out.println("========== PrivateShowGenerated.drl ==========");
        System.out.println(generatedPrivateShow);
        System.out.println("========== END ==========");
        kfs.write("src/main/resources/rules/PrivateShowGenerated.drl", generatedPrivateShow);

        String generatedGameState = TemplateLoader.generateGameStateRules();

        System.out.println("========== GameStateGenerated.drl ==========");
        System.out.println(generatedGameState);
        System.out.println("========== END ==========");
        kfs.write("src/main/resources/rules/GameStateGenerated.drl", generatedGameState);

        String generatedSuggest = TemplateLoader.generateSuggestCardRules();
        System.out.println("========== SuggestCardGenerated.drl ==========");
        System.out.println(generatedSuggest);
        System.out.println("========== END ==========");
        kfs.write("src/main/resources/rules/SuggestCardGenerated.drl",generatedSuggest);

        kfs.writeKModuleXML(new String(
                getClass().getResourceAsStream("/META-INF/kmodule.xml").readAllBytes()
        ));

        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException(kb.getResults().toString());
        }
        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }
}