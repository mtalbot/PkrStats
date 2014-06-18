/**
 * Perfoms G+ authentication
 */

function signinCallback(authResult) {
  if (authResult['status']['signed_in']) {
    $("#authID").val(authResult.id_token);
	$("#authCode").val(authResult.code);
    $("#authType").val("g+");
    $("#authForm").submit();
  } else {
    // Update the app to reflect a signed out user
    // Possible error values:
    //   "user_signed_out" - User is signed-out
    //   "access_denied" - User denied access to your app
    //   "immediate_failed" - Could not automatically log in the user
    console.log('Sign-in state: ' + authResult['error']);
  }
}