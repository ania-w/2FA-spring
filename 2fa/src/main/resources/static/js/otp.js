$(document).ready(function() {

  $('#otpForm').submit(function(event) {
    event.preventDefault();

    var formData = JSON.stringify({
      otp: $('input[name="otp"]').val(),
    });

    $.ajax({
      url: '/api/auth/otp',
      type: 'POST',
      contentType: 'application/json',
      data: formData,
      success: function() {
        window.location.href = '/api/content/index';
      },
      error: function() {
        alert('Invalid OTP');
      }
    });
  });
});