$(document).ready(function() {
   "use strict";

   // For Modal
   $(".modal").modal();

   // Close other sidenav on click of any sidenav
   if ($(window).width() > 900) {
      $("#email-sidenav").removeClass("sidenav");
   }

   // Toggle class of sidenav
   $("#email-sidenav").sidenav({
      onOpenStart: function() {
         $("#sidebar-list").addClass("sidebar-show");
      },
      onCloseEnd: function() {
         $("#sidebar-list").removeClass("sidebar-show");
      }
   });

   //  Notifications & messages scrollable
   if ($("#sidebar-list").length > 0) {
      var ps_sidebar_list = new PerfectScrollbar("#sidebar-list", {
         theme: "dark"
      });
   }
   if ($(".app-email .collection").length > 0) {
      var ps_email_collection = new PerfectScrollbar(".app-email .collection", {
         theme: "dark"
      });
   }

   $(".email-sidenav li").click(function() {
      $("li").removeClass("active");
      $(this).addClass("active");
   });

   // Remove Row
   $('.app-email i[type="button"]').click(function(e) {
      $(this)
         .closest("tr")
         .remove();
   });

   // Favorite star click
   $(".app-email .favorite i").on("click", function(e) {
      e.preventDefault();
      $(this).toggleClass("amber-text");
   });

   // Important label click
   $(".app-email .email-label i").on("click", function(e) {
      e.preventDefault();
      $(this).toggleClass("amber-text");
      if ($(this).text() == "label_outline") $(this).text("label");
      else $(this).text("label_outline");
   });

   // To delete all mails
   $(".app-email .delete-mails").on("click", function() {
      $(".collection-item")
         .find("input:checked")
         .closest(".collection-item")
         .remove();
   });

   // To delete Single mail
   $(".app-email .delete-task").on("click", function() {
      $(this)
         .closest(".collection-item")
         .remove();
   });

   // Sidenav
   $(".sidenav-trigger").on("click", function() {
      if ($(window).width() < 960) {
         $(".sidenav").sidenav("close");
         $(".app-sidebar").sidenav("close");
      }
   });
});

// Checkbox
function toggle(source) {
   checkboxes = document.getElementsByName("messageckb");
   // console.log(checkboxes);
   for (var i = 0, n = checkboxes.length; i < n; i++) {
      checkboxes[i].checked = source.checked;
      // console.log(checkboxes[i]);
   }
}

$(window).on("resize", function() {
   resizetable();

   if ($(window).width() > 899) {
      $("#email-sidenav").removeClass("sidenav");
   }

   if ($(window).width() < 900) {
      $("#email-sidenav").addClass("sidenav");
   }
});
function resizetable() {
   $(".app-email .collection").css({
      maxHeight: $(window).height() - 280 + "px"
   });
}
resizetable();
