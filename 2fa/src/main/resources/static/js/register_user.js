$(document).ready(function() {
  $('#registerForm').submit(function(event) {
    event.preventDefault();

    var message = document.getElementById("error-register");

    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        message.innerHTML = "Passwords do not match!";
        return;
    }

    const formData = new FormData();
    formData.append("username", username);
    formData.append("password", password);

    $.ajax({
      url: '/api/auth/register-user',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(Object.fromEntries(formData.entries())),
      processData: false,
      success: function() {
        var message = "Your account has been created. You can now log in.";
        window.location.href = '/api/auth/login?message=' + encodeURIComponent(message);
      },
      error: function() {
        alert('Error when creating new account');
      }
    });
  });
});
