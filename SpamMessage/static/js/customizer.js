/*
 * Theme customizer
 */

var menuBgDefault = false;

$(document).ready(function() {
   // Trigger customizer options
   $(".theme-cutomizer").sidenav({
      edge: "right"
   });

   var ps_theme_customiser = new PerfectScrollbar(".theme-cutomizer", {
      suppressScrollX: true
   });

   if ($("body").hasClass("vertical-modern-menu") || $("body").hasClass("vertical-menu-nav-dark")) {
      $(".menu-bg-color").hide();
   } else if ($("body").hasClass("vertical-gradient-menu") || $("body").hasClass("vertical-dark-menu")) {
      $(".menu-color").hide();
      menuBgDefault = true;
   } else if ($("body").hasClass("horizontal-menu")) {
      $(".menu-options").hide();
   }

   // Menu Options
   // ------------

   //Set menu color on select color
   $(".menu-color-option, .menu-bg-color-option").click(function(e) {
      $(".menu-color .menu-color-option, .menu-bg-color .menu-bg-color-option").removeClass("selected");
      $(this).addClass("selected");
      var menu_color = $(this).attr("data-color");
      if (menuBgDefault) {
         menuDark(true);
         menuBGColor(menu_color);
      } else {
         menuColor(menu_color);
      }
   });

   //Set menu dark/light
   $(".menu-dark-checkbox").click(function(e) {
      if ($(".menu-dark-checkbox").prop("checked")) {
         menuDark(true);
      } else {
         menuDark(false);
      }
   });

   //Set menu selection type on select
   $(".menu-selection-radio").click(function(e) {
      var menu_selection = $(this).val();
      menuSelection(menu_selection);
   });

   //Set menu selection type on select
   $(".menu-collapsed-checkbox").click(function(e) {
      if ($(".menu-collapsed-checkbox").prop("checked")) {
         menuCollapsed(true);
      } else {
         menuCollapsed(false);
      }
   });

   //Function to set menu color
   function menuColor(menu_color) {
      removeColorClass(".sidenav-main .sidenav li a.active");
      $(".sidenav-main .sidenav li a.active").css({ background: "none", "box-shadow": "none" });
      $(".sidenav-main .sidenav li a.active").addClass(menu_color + " gradient-shadow");
   }

   //Function to set  menu bg color
   function menuBGColor(menu_color) {
      removeColorClass(".sidenav-main");
      $(".sidenav-main").addClass(menu_color + " sidenav-gradient");
   }

   //Function menu dark/light
   function menuDark(isDark) {
      if (isDark) {
         $(".menu-dark-checkbox").prop("checked", true);
         $(".sidenav-main")
            .removeClass("sidenav-light")
            .addClass("sidenav-dark");
      } else {
         $(".menu-dark-checkbox").prop("checked", false);
         $(".sidenav-main")
            .addClass("sidenav-light")
            .removeClass("sidenav-dark");
      }
   }

   //Function menu collapsed
   function menuCollapsed(isCollapsed) {
      if (isCollapsed) {
         $(".sidenav-main").removeClass("nav-lock");
         $(".navbar-main.nav-collapsible")
            .removeClass("sideNav-lock")
            .addClass("nav-expanded");
         $(".navbar-toggler i").html("radio_button_unchecked");
         $("#main").addClass("main-full");
         $(".sidenav-main.nav-collapsible, .navbar .brand-sidebar").trigger("mouseleave");
      } else {
         $(".sidenav-main")
            .addClass("nav-lock")
            .removeClass("nav-collapsed");
         $(".navbar-main.nav-collapsible")
            .addClass("sideNav-lock")
            .removeClass("nav-collapsed");
         $(".navbar-toggler i").html("radio_button_checked");
         $("#main").removeClass("main-full");
         $(".sidenav-main.nav-collapsible, .navbar .brand-sidebar").trigger("mouseenter");
      }
   }

   //Function menu collapsed
   function menuSelection(menu_selection) {
      $(".sidenav-main")
         .removeClass("sidenav-active-square sidenav-active-rounded")
         .addClass(menu_selection);
   }

   // Navbar Options
   // --------------

   // On click of navbar color
   $(".navbar-color-option").click(function(e) {
      $(".navbar-color .navbar-color-option").removeClass("selected");
      $(this).addClass("selected");
      var navbar_color = $(this).attr("data-color");
      navbarDark(true);
      navbarColor(navbar_color);
   });

   //Set menu dark/light
   $(".navbar-dark-checkbox").click(function(e) {
      if ($(".navbar-dark-checkbox").prop("checked")) {
         navbarDark(true);
      } else {
         navbarDark(false);
      }
   });

   // Click on navbar fixed checkbox
   $(".navbar-fixed-checkbox").click(function(e) {
      if ($(".navbar-fixed-checkbox").prop("checked")) {
         $("#header .navbar").addClass("navbar-fixed");
      } else {
         $("#header .navbar").removeClass("navbar-fixed");
      }
   });

   //Function to set navbar dark checkbox
   function navbarDark(isDark) {
      removeColorClass(".navbar-main");
      if (isDark) {
         $(".navbar-dark-checkbox").prop("checked", true);
         $(".navbar-main")
            .removeClass("navbar-light")
            .addClass("navbar-dark");
      } else {
         $(".navbar-dark-checkbox").prop("checked", false);
         $(".navbar-main")
            .addClass("navbar-light")
            .removeClass("navbar-dark");
      }
   }

   //Function to set  navbar color
   function navbarColor(navbar_color) {
      removeColorClass(".navbar-main");
      $(".navbar-main").addClass(navbar_color);
      if ($("body").hasClass("vertical-modern-menu")) {
         removeColorClass(".content-wrapper-before");
         $(".content-wrapper-before").addClass(navbar_color);
      }
   }

   // Footer Options
   // --------------

   //On click of footer dark
   $(".footer-dark-checkbox").click(function(e) {
      removeColorClass(".page-footer");
      if ($(".footer-dark-checkbox").prop("checked")) {
         footerDark(true);
      } else {
         footerDark(false);
      }
   });

   // Click on footer fixed checkbox
   $(".footer-fixed-checkbox").click(function(e) {
      if ($(".footer-fixed-checkbox").prop("checked")) {
         $(".page-footer")
            .addClass("footer-fixed")
            .removeClass("footer-static");
      } else {
         $(".page-footer")
            .removeClass("footer-fixed")
            .addClass("footer-static");
      }
   });

   //Function to set footer dark checkbox
   function footerDark(isDark) {
      if (isDark) {
         $(".footer-dark-checkbox").prop("checked", true);
         $(".page-footer")
            .removeClass("footer-light")
            .addClass("footer-dark");
      } else {
         $(".footer-dark-checkbox").prop("checked", false);
         $(".page-footer")
            .addClass("footer-light")
            .removeClass("footer-dark");
      }
   }

   //Function to remove default color
   function removeColorClass(el) {
      $(el).removeClass(
         "gradient-45deg-indigo-blue gradient-45deg-purple-deep-orange gradient-45deg-light-blue-cyan gradient-45deg-purple-amber gradient-45deg-purple-deep-purple gradient-45deg-deep-orange-orange gradient-45deg-green-teal gradient-45deg-indigo-light-blue gradient-45deg-red-pink red purple pink deep-purple cyan teal light-blue amber darken-3 brown darken-2 gradient-45deg-indigo-purple gradient-45deg-deep-purple-blue"
      );
   }
});
