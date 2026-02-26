package com.hasini.pipelineiq.cli;

import com.hasini.pipelineiq.core.classify.FailureClassifier;
import com.hasini.pipelineiq.core.model.AnalysisResult;
import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.parse.LogParser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final LogParser logParser;
    private final FailureClassifier classifier;


    @Override
    public void run(String... args) throws Exception {

        System.out.println("DEBUG: AnalyzerRunner has started!");
        if(args.length==0){
            System.out.println("Please enter the path to the log file");
            return;
        }

        Path path = Paths.get(args[0]);
        String errorSnippet = logParser.extractErrorSnippet(path);
        FailureCategory failure = classifier.classify(errorSnippet);
        AnalysisResult result = AnalysisResult.initial(failure,"tool",errorSnippet);
        System.out.println("Analysis Done: "+ result);

    }
}
