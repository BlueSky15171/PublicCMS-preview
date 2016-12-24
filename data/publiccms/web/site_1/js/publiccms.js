$(function() {
	$('.cms').each(function() {
		switch ($(this).data('action')) {
		case 'login':
		case 'register':
			$(this).submit(function() {
				var returnUrl = $(this).data('return-url');
				$.ajax({
					type : this.method || 'POST', url : $(this).attr("action"), data : $(this).serializeArray(),
					dataType : "json", cache : false, success : function(data) {
						if (data.loginInfo ) {
							$.cookie.raw = true;
							$.cookie('PUBLICCMS_USER', data.loginInfo, {
								expires : 10800
							});
							window.location.href = returnUrl;
						} else if (data.error ) {
							if (window.location.href.indexOf('?') > 0 ) {
								window.location.href = window.location.href + "&error=" + data.error;
							} else {
								window.location.href = window.location.href + "?error=" + data.error;
							}
						} else if (data.message ) {
							if (window.location.href.indexOf('?') > 0 ) {
								window.location.href = window.location.href + "&message=" + data.message;
							} else {
								window.location.href = window.location.href + "?message=" + data.message;
							}
						}
					}
				});
				return false;
			});
			break;
		case 'logout':
			$(this).click(function() {
				var returnUrl = $(this).data('return-url');
				$.getJSON($(this).attr("url"), function() {
					window.location.href = returnUrl;
				});
				return false;
			});
			break;
		}
	});
});