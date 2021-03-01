package com.intellij.lang.jsgraphql.types.schema.validation;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
public enum SchemaValidationErrorType {

    UnbrokenInputCycle,
    ObjectDoesNotImplementItsInterfaces,
    ImplementingTypeLackOfFieldError,
    InputObjectTypeLackOfFieldError,
    EnumLackOfValueError,
    UnionTypeLackOfTypeError,
    InvalidUnionMemberTypeError,
    InvalidCustomizedNameError,
    NonNullWrapNonNullError,
    RepetitiveElementError

}
