package com.ftn.sbnz.service.config;

import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class TemplateLoader {

    public static String generateNoShowRules() {
        List<NoShowRow> rows = Arrays.asList(
                new NoShowRow("suspect", "getSuspect"),
                new NoShowRow("weapon",  "getWeapon"),
                new NoShowRow("room",    "getRoom")
        );

        InputStream template = TemplateLoader.class
                .getResourceAsStream("/rules/NoShowTemplate.drt");

        ObjectDataCompiler compiler = new ObjectDataCompiler();
        return compiler.compile(rows, template);
    }

    public static String generatePrivateShowRules() {
        List<PrivateShowRow> rows = Arrays.asList(
                new PrivateShowRow("suspect", "$wCard", "$rCard", "$sCard"),
                new PrivateShowRow("weapon",  "$sCard", "$rCard", "$wCard"),
                new PrivateShowRow("room",    "$sCard", "$wCard", "$rCard")
        );

        InputStream template = TemplateLoader.class
                .getResourceAsStream("/rules/PrivateShowTemplate.drt");

        ObjectDataCompiler compiler = new ObjectDataCompiler();
        return compiler.compile(rows, template);
    }

    public static String generateGameStateRules() {
        List<GameStateRow> rows = Arrays.asList(
                new GameStateRow("0.0", "0.3", "EARLY_GAME"),
                new GameStateRow("0.3", "0.7", "MID_GAME"),
                new GameStateRow("0.7", "1.01", "LATE_GAME")
        );

        InputStream template = TemplateLoader.class
                .getResourceAsStream("/rules/GameStateTemplate.drt");

        ObjectDataCompiler compiler = new ObjectDataCompiler();
        return compiler.compile(rows, template);
    }

    public static String generateSuggestCardRules() {

        List<SuggestCardRow> rows = Arrays.asList(

                new SuggestCardRow(
                        "SUSPECT",
                        "and not Owns(card == $c)",
                        "not Owns(card == $card)"
                ),

                new SuggestCardRow(
                        "WEAPON",
                        "and not Owns(card == $c)",
                        "not Owns(card == $card)"
                ),

                new SuggestCardRow(
                        "ROOM",
                        "",
                        ""
                )
        );
        InputStream template = TemplateLoader.class
                .getResourceAsStream("/rules/SuggestCardTemplate.drt");

        ObjectDataCompiler compiler = new ObjectDataCompiler();

        return compiler.compile(rows, template);
    }

    public static KieContainer buildContainerWithTemplate() throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        // ucitaj sve postojece resurse iz classpath-a (kmodule, .drl)
        kfs.writeKModuleXML(
                new String(TemplateLoader.class
                        .getResourceAsStream("/META-INF/kmodule.xml").readAllBytes())
        );


        String generatedNoShow = TemplateLoader.generateNoShowRules();
        kfs.write("src/main/resources/rules/NoShowGenerated.drl", generatedNoShow);

        String generatedPrivateShow = TemplateLoader.generatePrivateShowRules();
        kfs.write("src/main/resources/rules/PrivateShowGenerated.drl", generatedPrivateShow);

        System.out.println("=== PrivateShowGenerated.drl ===");
        System.out.println(generatedPrivateShow);
        System.out.println("=== END ===");

        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException(kb.getResults().toString());
        }
        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }
}