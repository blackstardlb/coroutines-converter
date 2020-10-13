package nl.blackstardlb.gradle.coroutinesconverter.impl

enum class WrapperType(val originalFunctionCallWrapper: (String) -> OriginalFunctionCallWrapper) {
    FUTURES(
        { FutureOriginalFunctionCallWrapperImpl(it) },
    ),
    REACTOR(
        { ReactorOriginalFunctionCallWrapperImpl(it) },
    )
}
