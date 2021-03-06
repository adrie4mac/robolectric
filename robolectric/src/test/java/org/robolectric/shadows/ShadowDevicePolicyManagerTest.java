package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowDevicePolicyManager}. */
@RunWith(TestRunners.MultiApiSelfTest.class)
public final class ShadowDevicePolicyManagerTest {

  private DevicePolicyManager devicePolicyManager;
  private ShadowDevicePolicyManager shadowDevicePolicyManager;
  private UserManager userManager;
  private ComponentName testComponent;

  @Before
  public void setUp() {
    devicePolicyManager =
        (DevicePolicyManager)
            RuntimeEnvironment.application.getSystemService(Context.DEVICE_POLICY_SERVICE);
    shadowDevicePolicyManager = shadowOf(devicePolicyManager);

    userManager =
        (UserManager) RuntimeEnvironment.application.getSystemService(Context.USER_SERVICE);

    testComponent = new ComponentName("com.example.app", "DeviceAdminReceiver");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void isDeviceOwnerAppShouldReturnFalseForNonDeviceOwnerApp() {
    // GIVEN an test package which is not the device owner app of the device
    String testPackage = testComponent.getPackageName();

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDeviceOwnerShouldReturnFalseForProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowDevicePolicyManager.setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void isDeviceOwnerShouldReturnTrueForDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerAppShouldReturnFalseForNonProfileOwnerApp() {
    // GIVEN an test package which is not the profile owner app of the device
    String testPackage = testComponent.getPackageName();

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerShouldReturnFalseForDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerShouldReturnTrueForProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowDevicePolicyManager.setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isTrue();
  }

  @Test
  public void isAdminActiveShouldReturnFalseForNonAdminDevice() {
    // GIVEN a test component which is not an active admin of the device
    // WHEN DevicePolicyManager#isAdminActive is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isAdminActive(testComponent)).isFalse();
  }

  @Test
  public void isAdminActiveShouldReturnTrueForAnyDeviceAdminDevice() {
    // GIVEN a test component which is an active admin of the device
    shadowDevicePolicyManager.setActiveAdmin(testComponent);

    // WHEN DevicePolicyManager#isAdminActive is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isAdminActive(testComponent)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getActiveAdminsShouldReturnDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#getActiveAdmins is called
    // THEN the return of the method should include the device owner app
    assertThat(devicePolicyManager.getActiveAdmins()).contains(testComponent);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getActiveAdminsShouldReturnProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    shadowDevicePolicyManager.setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#getActiveAdmins is called
    // THEN the return of the method should include the profile owner app
    assertThat(devicePolicyManager.getActiveAdmins()).contains(testComponent);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addUserRestrictionShouldWorkAsIntendedForDeviceOwner() {
    // GIVEN a user restriction to set
    String restrictionKey = "restriction key";

    // GIVEN the caller is the device owner
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#addUserRestriction is called with the key
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be set for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addUserRestrictionShouldWorkAsIntendedForProfileOwner() {
    // GIVEN a user restriction to set
    String restrictionKey = "restriction key";

    // GIVEN the caller is the profile owner
    shadowDevicePolicyManager.setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#addUserRestriction is called with the key
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be set for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearUserRestrictionShouldWorkAsIntendedForActiveAdmins() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN a user restriction has set
    String restrictionKey = "restriction key";
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // WHEN DevicePolicyManager#clearUserRestriction is called with the key
    devicePolicyManager.clearUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be cleared for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnFalseForAppsByDefault() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it's never be set hidden or non hidden
    String app = "com.example.non.hidden";

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return false
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnTrueForHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it is hidden
    String hiddenApp = "com.example.hidden";
    devicePolicyManager.setApplicationHidden(testComponent, hiddenApp, true);

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return true
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, hiddenApp)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnFalseForNonHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it is not hidden
    String nonHiddenApp = "com.example.non.hidden";
    devicePolicyManager.setApplicationHidden(testComponent, nonHiddenApp, false);

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return false
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, nonHiddenApp)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationHiddenShouldBeAbleToUnhideHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it is hidden
    String app = "com.example.hidden";
    devicePolicyManager.setApplicationHidden(testComponent, app, true);

    // WHEN DevicePolicyManager#setApplicationHidden is called on the app to unhide it
    devicePolicyManager.setApplicationHidden(testComponent, app, false);

    // THEN the app shouldn't be hidden anymore
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnFalseForPackageNeverHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it's never be set hidden or non hidden
    String app = "com.example.non.hidden";

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return false
    assertThat(shadowDevicePolicyManager.wasPackageEverHidden(app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnTrueForPackageWhichIsHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it's hidden
    String hiddenApp = "com.example.hidden";
    devicePolicyManager.setApplicationHidden(testComponent, hiddenApp, true);

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return true
    assertThat(shadowDevicePolicyManager.wasPackageEverHidden(hiddenApp)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnTrueForPackageWhichWasHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app and it was hidden
    String app = "com.example.hidden";
    devicePolicyManager.setApplicationHidden(testComponent, app, true);
    devicePolicyManager.setApplicationHidden(testComponent, app, false);

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return true
    assertThat(shadowDevicePolicyManager.wasPackageEverHidden(app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void enableSystemAppShouldWorkForActiveAdmins() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN a system app
    String app = "com.example.system";

    // WHEN DevicePolicyManager#enableSystemApp is called with the app
    devicePolicyManager.enableSystemApp(testComponent, app);

    // THEN the app should be enabled
    assertThat(shadowDevicePolicyManager.wasSystemAppEnabled(app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnFalseForAppsNeverBeingBlocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app
    String app = "com.example.app";

    // WHEN DevicePolicyManager#isUninstallBlocked is called with the app
    // THEN it should return false
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnTrueForAppsBeingUnblocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app which is blocked from being uninstalled
    String app = "com.example.app";
    devicePolicyManager.setUninstallBlocked(testComponent, app, true);

    // WHEN DevicePolicyManager#UninstallBlocked is called with the app
    // THEN it should return true
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnFalseForAppsBeingBlocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app which is unblocked from being uninstalled
    String app = "com.example.app";
    devicePolicyManager.setUninstallBlocked(testComponent, app, true);
    devicePolicyManager.setUninstallBlocked(testComponent, app, false);

    // WHEN DevicePolicyManager#UninstallBlocked is called with the app
    // THEN it should return false
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationRestrictionsShouldWorkAsIntendedForDeviceOwner() {
    // GIVEN the caller is the device owner
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an application restriction bundle
    Bundle restrictions = new Bundle();
    restrictions.putString("key", "value");

    // GIVEN an app which the restriction is set to
    String app = "com.example.app";

    // WHEN DevicePolicyManager#setApplicationRestrictions is called to set the restrictions to the
    // app
    devicePolicyManager.setApplicationRestrictions(testComponent, app, restrictions);

    // THEN the restrictions should be set correctly
    Bundle actualRestrictions = devicePolicyManager.getApplicationRestrictions(testComponent, app);
    assertThat(actualRestrictions.getString("key", "default value")).isEqualTo("value");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationRestrictionsShouldWorkAsIntendedForProfileOwner() {
    // GIVEN the caller is the profile owner
    shadowDevicePolicyManager.setProfileOwner(testComponent);

    // GIVEN an application restriction bundle
    Bundle restrictions = new Bundle();
    restrictions.putString("key", "value");

    // GIVEN an app which the restriction is set to
    String app = "com.example.app";

    // WHEN DevicePolicyManager#setApplicationRestrictions is called to set the restrictions to the
    // app
    devicePolicyManager.setApplicationRestrictions(testComponent, app, restrictions);

    // THEN the restrictions should be set correctly
    Bundle actualRestrictions = devicePolicyManager.getApplicationRestrictions(testComponent, app);
    assertThat(actualRestrictions.getString("key", "default value")).isEqualTo("value");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getApplicationRestrictionsShouldReturnEmptyBundleIfAppHasNone() {
    // GIVEN the caller is the device owner
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN an app has no restrictions
    String app = "com.example.app";

    // WHEN DevicePolicyManager#getApplicationRestrictions is called to get the restrictions of the
    // app
    // THEN it should return the empty bundle
    assertThat(devicePolicyManager.getApplicationRestrictions(testComponent, app))
        .isEqualTo(Bundle.EMPTY);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldReturnNothingWhenNoAccountIsDislabed() {
    // GIVEN no account type has ever been disabled

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should be empty
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled()).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldReturnDisabledAccountTypesIfAny() {
    // GIVEN the caller is the device owner
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN a disabled account type
    String disabledAccountType = "com.example.account.type";
    devicePolicyManager.setAccountManagementDisabled(testComponent, disabledAccountType, true);

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should contain the disabled account type
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled())
        .isEqualTo(new String[] {disabledAccountType});
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldNotReturnReenabledAccountTypesIfAny() {
    // GIVEN the caller is the device owner
    shadowDevicePolicyManager.setDeviceOwner(testComponent);

    // GIVEN a re-enabled account type
    String reenabledAccountType = "com.example.account.type";
    devicePolicyManager.setAccountManagementDisabled(testComponent, reenabledAccountType, true);
    devicePolicyManager.setAccountManagementDisabled(testComponent, reenabledAccountType, false);

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should not contain the re-enabled account type
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled()).isEmpty();
  }
}
