package com.intellij.lang.jsgraphql.types.validation;


public enum ValidationErrorType {

    DefaultForNonNullArgument,
    WrongType,
    UnknownType,
    SubSelectionRequired,
    SubSelectionNotAllowed,
    InvalidSyntax,
    BadValueForDefaultArg,
    FieldUndefined,
    InlineFragmentTypeConditionInvalid,
    FragmentTypeConditionInvalid,
    UnknownArgument,
    UndefinedFragment,
    NonInputTypeOnVariable,
    UnusedFragment,
    MissingFieldArgument,
    MissingDirectiveArgument,
    VariableTypeMismatch,
    UnknownDirective,
    MisplacedDirective,
    UndefinedVariable,
    UnusedVariable,
    FragmentCycle,
    FieldsConflict,
    InvalidFragmentType,
    LoneAnonymousOperationViolation,
    NonExecutableDefinition,
    DuplicateOperationName,
    DuplicateFragmentName,
    DuplicateDirectiveName,
    DuplicateArgumentNames,
    DuplicateVariableName,
    NullValueForNonNullArgument
}
