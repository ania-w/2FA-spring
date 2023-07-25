$(document).ready(function() {

 $('#sendEmail').click(function(event) {
     var qrCodeSrc = $('#qrCodeImage').attr('src');
     var qrCodeData = qrCodeSrc.replace('data:image/png;base64,', '');
     var email = document.getElementById("email").value;

    const formData = new FormData();
    formData.append("qrCodeData", qrCodeData);
    formData.append("email", email);

    $.ajax({
                url: '/api/auth/send-email',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(Object.fromEntries(formData.entries())),
                processData: false,
                success: function() {
                      $('#sendEmail').prop('disabled', true);
                      $('#sendEmail').css('background-color', 'grey');
                      $('#sendEmail').val("Email sent");
                },
    });
});

  $('#registerForm').submit(function(event) {
    event.preventDefault();

    var message = document.getElementById("message");

    var username = document.getElementById("username").value;
    var email = document.getElementById("email").value;
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        message.innerHTML = "Passwords do not match!";
        return;
    }

    const formData = new FormData();
    formData.append("username", username);
    formData.append("password", password);
    formData.append("email", email);

    $.ajax({
          url: '/api/auth/register-user',
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(Object.fromEntries(formData.entries())),
          processData: false,
          success: function(response) {
              var qrCodeData = response.qrCode;
              var message = "User account created successfully."
              $('#qrCodeImage').attr('src', 'data:image/png;base64,' + qrCodeData);
              $('#qrCodeContainer').css("display","block");
              $('#registerForm').css("display","none");

              $('#activate2fa').click(function(event) {
              const form = new FormData();
              form.append("username", username);
              form.append("email", email);
                $.ajax({
                 url: '/api/auth/enable-2fa',
                 type: 'POST',
                 contentType: 'application/json',
                 data: JSON.stringify(Object.fromEntries(form.entries())),
                 processData: false,
                    success: function() {
                        window.location.href = '/api/auth/login?message=' + encodeURIComponent(message);
                        },
                    error: function() {
                         window.location.href = '/api/auth/login?message=' + encodeURIComponent("Error when activating two factor authentication. Try logging in and activating it manually.");
                    }
                 });

             });
          },
          error: function() {
            alert('Error when creating new account');
          }
        });
});
});