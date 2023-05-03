$(document).ready(function() {
  $('#loginForm').submit(function(event) {
    event.preventDefault();

    var formData = JSON.stringify({
      username: $('input[name="username"]').val(),
      password: $('input[name="password"]').val()
    });

    $.ajax({
      url: '/api/auth/login',
      type: 'POST',
      contentType: 'application/json',
      data: formData,
      success: function() {
        window.location.href = '/api/content/index';
      },
      error: function() {
        alert('Login failed');
      }
    });
  });
});