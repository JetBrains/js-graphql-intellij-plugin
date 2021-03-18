package com.intellij.lang.jsgraphql.types.validation;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.validation.rules.*;

import java.util.ArrayList;
import java.util.List;

@Internal
public class Validator {

    public List<ValidationError> validateDocument(GraphQLSchema schema, Document document) {
        ValidationContext validationContext = new ValidationContext(schema, document);


        ValidationErrorCollector validationErrorCollector = new ValidationErrorCollector();
        List<AbstractRule> rules = createRules(validationContext, validationErrorCollector);
        LanguageTraversal languageTraversal = new LanguageTraversal();
        languageTraversal.traverse(document, new RulesVisitor(validationContext, rules));

        return validationErrorCollector.getErrors();
    }

    public List<AbstractRule> createRules(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        List<AbstractRule> rules = new ArrayList<>();

        ExecutableDefinitions executableDefinitions = new ExecutableDefinitions(validationContext, validationErrorCollector);
        rules.add(executableDefinitions);

        ArgumentsOfCorrectType argumentsOfCorrectType = new ArgumentsOfCorrectType(validationContext, validationErrorCollector);
        rules.add(argumentsOfCorrectType);

        FieldsOnCorrectType fieldsOnCorrectType = new FieldsOnCorrectType(validationContext, validationErrorCollector);
        rules.add(fieldsOnCorrectType);
        FragmentsOnCompositeType fragmentsOnCompositeType = new FragmentsOnCompositeType(validationContext, validationErrorCollector);
        rules.add(fragmentsOnCompositeType);

        KnownArgumentNames knownArgumentNames = new KnownArgumentNames(validationContext, validationErrorCollector);
        rules.add(knownArgumentNames);
        KnownDirectives knownDirectives = new KnownDirectives(validationContext, validationErrorCollector);
        rules.add(knownDirectives);
        KnownFragmentNames knownFragmentNames = new KnownFragmentNames(validationContext, validationErrorCollector);
        rules.add(knownFragmentNames);
        KnownTypeNames knownTypeNames = new KnownTypeNames(validationContext, validationErrorCollector);
        rules.add(knownTypeNames);

        NoFragmentCycles noFragmentCycles = new NoFragmentCycles(validationContext, validationErrorCollector);
        rules.add(noFragmentCycles);
        NoUndefinedVariables noUndefinedVariables = new NoUndefinedVariables(validationContext, validationErrorCollector);
        rules.add(noUndefinedVariables);
        NoUnusedFragments noUnusedFragments = new NoUnusedFragments(validationContext, validationErrorCollector);
        rules.add(noUnusedFragments);
        NoUnusedVariables noUnusedVariables = new NoUnusedVariables(validationContext, validationErrorCollector);
        rules.add(noUnusedVariables);

        OverlappingFieldsCanBeMerged overlappingFieldsCanBeMerged = new OverlappingFieldsCanBeMerged(validationContext, validationErrorCollector);
        rules.add(overlappingFieldsCanBeMerged);

        PossibleFragmentSpreads possibleFragmentSpreads = new PossibleFragmentSpreads(validationContext, validationErrorCollector);
        rules.add(possibleFragmentSpreads);
        ProvidedNonNullArguments providedNonNullArguments = new ProvidedNonNullArguments(validationContext, validationErrorCollector);
        rules.add(providedNonNullArguments);

        ScalarLeafs scalarLeafs = new ScalarLeafs(validationContext, validationErrorCollector);
        rules.add(scalarLeafs);

        VariableDefaultValuesOfCorrectType variableDefaultValuesOfCorrectType = new VariableDefaultValuesOfCorrectType(validationContext, validationErrorCollector);
        rules.add(variableDefaultValuesOfCorrectType);
        VariablesAreInputTypes variablesAreInputTypes = new VariablesAreInputTypes(validationContext, validationErrorCollector);
        rules.add(variablesAreInputTypes);
        VariableTypesMatchRule variableTypesMatchRule = new VariableTypesMatchRule(validationContext, validationErrorCollector);
        rules.add(variableTypesMatchRule);

        LoneAnonymousOperation loneAnonymousOperation = new LoneAnonymousOperation(validationContext, validationErrorCollector);
        rules.add(loneAnonymousOperation);

        UniqueOperationNames uniqueOperationNames = new UniqueOperationNames(validationContext, validationErrorCollector);
        rules.add(uniqueOperationNames);

        UniqueFragmentNames uniqueFragmentNames = new UniqueFragmentNames(validationContext, validationErrorCollector);
        rules.add(uniqueFragmentNames);

        UniqueDirectiveNamesPerLocation uniqueDirectiveNamesPerLocation = new UniqueDirectiveNamesPerLocation(validationContext, validationErrorCollector);
        rules.add(uniqueDirectiveNamesPerLocation);

        UniqueArgumentNamesRule uniqueArgumentNamesRule = new UniqueArgumentNamesRule(validationContext, validationErrorCollector);
        rules.add(uniqueArgumentNamesRule);

        UniqueVariableNamesRule uniqueVariableNamesRule = new UniqueVariableNamesRule(validationContext, validationErrorCollector);
        rules.add(uniqueVariableNamesRule);

        return rules;
    }
}