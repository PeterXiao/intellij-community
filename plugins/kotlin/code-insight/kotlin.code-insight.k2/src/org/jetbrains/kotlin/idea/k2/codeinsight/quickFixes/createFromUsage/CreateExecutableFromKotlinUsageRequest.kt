// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.k2.codeinsight.quickFixes.createFromUsage

import com.intellij.lang.jvm.JvmModifier
import com.intellij.lang.jvm.actions.AnnotationRequest
import com.intellij.lang.jvm.actions.CreateExecutableRequest
import com.intellij.lang.jvm.actions.ExpectedParameter
import com.intellij.psi.PsiJvmSubstitutor
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer

internal abstract class CreateExecutableFromKotlinUsageRequest<out T : KtCallElement>(
    call: T,
    private val modifiers: Collection<JvmModifier>,
) : CreateExecutableRequest {
    private val project = call.project
    private val callPointer: SmartPsiElementPointer<T> = call.createSmartPointer()
    private val expectedParameterInfo: MutableList<ExpectedParameter> = mutableListOf()

    init {
        analyze(call) {
            call.valueArgumentList?.arguments?.forEachIndexed { index, valueArgument ->
                expectedParameterInfo.add(valueArgument.getExpectedParameterInfo(index))
            }
        }
    }

    internal val call: T get() = callPointer.element ?: error("dead pointer")

    override fun isValid(): Boolean = callPointer.element != null

    override fun getAnnotations(): List<AnnotationRequest> = emptyList()

    override fun getModifiers(): Collection<JvmModifier> = modifiers

    //todo substitutor
    override fun getTargetSubstitutor(): PsiJvmSubstitutor = PsiJvmSubstitutor(project, PsiSubstitutor.EMPTY)

    override fun getExpectedParameters(): List<ExpectedParameter> = expectedParameterInfo
}
