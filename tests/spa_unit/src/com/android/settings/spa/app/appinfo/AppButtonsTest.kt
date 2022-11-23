/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.spa.app.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ModuleInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.settingslib.applications.AppUtils
import com.android.settingslib.spa.testutils.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.MockitoSession
import org.mockito.Spy
import org.mockito.quality.Strictness
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidJUnit4::class)
class AppButtonsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockSession: MockitoSession

    @Spy
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Mock
    private lateinit var packageInfoPresenter: PackageInfoPresenter

    @Mock
    private lateinit var packageManager: PackageManager

    @Before
    fun setUp() {
        mockSession = ExtendedMockito.mockitoSession()
            .initMocks(this)
            .mockStatic(AppUtils::class.java)
            .strictness(Strictness.LENIENT)
            .startMocking()
        whenever(packageInfoPresenter.context).thenReturn(context)
        whenever(packageInfoPresenter.packageName).thenReturn(PACKAGE_NAME)
        whenever(packageInfoPresenter.userPackageManager).thenReturn(packageManager)
        doThrow(NameNotFoundException()).`when`(packageManager).getModuleInfo(PACKAGE_NAME, 0)
        whenever(packageManager.getPackageInfo(PACKAGE_NAME, 0)).thenReturn(PACKAGE_INFO)
        whenever(AppUtils.isMainlineModule(packageManager, PACKAGE_NAME)).thenReturn(false)
    }

    @After
    fun tearDown() {
        mockSession.finishMocking()
    }

    @Test
    fun isSystemModule_notDisplayed() {
        doReturn(ModuleInfo()).`when`(packageManager).getModuleInfo(PACKAGE_NAME, 0)

        setContent()

        composeTestRule.onRoot().assertIsNotDisplayed()
    }

    @Test
    fun isMainlineModule_notDisplayed() {
        whenever(AppUtils.isMainlineModule(packageManager, PACKAGE_NAME)).thenReturn(true)

        setContent()

        composeTestRule.onRoot().assertIsNotDisplayed()
    }

    @Test
    fun isNormalApp_displayed() {
        setContent()

        composeTestRule.onRoot().assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                whenever(packageInfoPresenter.flow).thenReturn(flowOf(PACKAGE_INFO).stateIn(scope))
            }

            AppButtons(packageInfoPresenter)
        }

        composeTestRule.delay()
    }

    private companion object {
        const val PACKAGE_NAME = "package.name"
        val PACKAGE_INFO = PackageInfo().apply {
            applicationInfo = ApplicationInfo().apply {
                packageName = PACKAGE_NAME
            }
            packageName = PACKAGE_NAME
        }
    }
}
