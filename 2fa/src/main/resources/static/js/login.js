$(document).ready(function() {

console.log(messageParam);

   var messageParam = $('#messageParam').val();

   var message = document.getElementById("message");

   if(messageParam != null)
       message.innerHTML = messageParam;

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
      success: function(response) {
      if(response=="activate")
        window.location.href = '/api/auth/activate';
      else
        window.location.href = '/api/auth/second-factor';
      },
      error: function() {
        window.location.href = '/api/auth/login'
                alert('Login failed');

      }
    });
  });
});