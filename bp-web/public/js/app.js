
'use strict';

var minorityApp =
  angular.module(
    'minorityApp',
    [
      'minorityApp.services',
      'minorityApp.controllers',
      'ngRoute',
      'ngResource',
      'chieffancypants.loadingBar',
      'ngCookies',
      'angularLocalStorage',
      'classy',
      'ngAngularError',
      'ngAnimate',
      'toaster',
      'angular-underscore',
      'ui.bootstrap',
      'ui.select',
      'ui.sortable',
      'ngDialog',
      'angularMoment',
      'ui.tree'
    ]
  );
minorityApp.filter('slice', function() {
  return function(arr, start, end) {
    return arr.slice(arr.length-start, end);
  };
});
minorityApp.config(['$routeProvider','$httpProvider', function($routeProvider) {
/*Credentials managment*/
     $routeProvider.when('/profile', {templateUrl: 'partials/forms/profile.html', controller: 'ProfileController'});
    // BProcess
/*I*/$routeProvider.when('/bprocesses', {templateUrl: 'partials/forms/bprocesses/bp-list.html', controller: 'BProcessListCtrl'});
/*C*/$routeProvider.when('/bprocess/new', {templateUrl: 'partials/forms/bprocesses/bp-new.html', controller: 'BPCreationCtrl'});
/*R*/$routeProvider.when('/bprocess/:id/show', {templateUrl: 'partials/forms/bprocesses/bp-detail.html', controller: 'BProcessDetailCtrl'});
/*a*/
    // Embed
/*I*/$routeProvider.when('/bprocess/:BPid/embed', {templateUrl: 'partials/forms/bp_elements/embeded.html', controller: 'BPEmbededCtrl'});
    // Proc Elements
/*I*/$routeProvider.when('/bprocess/:BPid/elements', {templateUrl: 'partials/forms/bp_elements/bpelem-list.html', controller: 'BPelementListCtrl'});
/*C*/$routeProvider.when('/bprocess/:BPid/element/new', {templateUrl: 'partials/forms/bp_elements/bpelem-new.html', controller: 'BPelementCreationCtrl'});
/*R*/$routeProvider.when('/bprocess/:BPid/element/:id/show', {templateUrl: 'partials/forms/bp_elements/bpelem-detail.html', controller: 'BPelementDetailCtrl'});
/*U*/$routeProvider.when('/bprocess/:BPid/element/:id/edit', {templateUrl: 'partials/forms/bp_elements/bpelem-edit.html', controller: 'BPelementDetailCtrl'});

    // Stations
/*I*/$routeProvider.when('/bprocess/:BPid/stations', {templateUrl: 'partials/forms/bpstations/bpstation-list.html', controller: 'BPstationListCtrl'});
/*C*/$routeProvider.when('/bprocess/:BPid/station/new', {templateUrl: 'partials/forms/bpstations/bpstation-new.html', controller: 'BPstationCreationCtrl'});
/*R*/$routeProvider.when('/bprocess/:BPid/station/:id/show', {templateUrl: 'partials/forms/bpstations/bpstation-detail.html', controller: 'BPstationDetailCtrl'});
/*U*/$routeProvider.when('/bprocess/:BPid/station/:id/edit', {templateUrl: 'partials/forms/bpstations/bpstation-edit.html', controller: 'BPstationDetailCtrl'});

   // Permissions
/*I*/$routeProvider.when('/bprocess/:BPid/perms', {templateUrl: 'partials/forms/perms/perm-list.html', controller: 'BPPermListCtrl'});
/*C*/$routeProvider.when('/bprocess/:BPid/perm/new', {templateUrl: 'partials/forms/perms/perm-new.html', controller: 'BPPermCreationCtrl'});
/*R*/$routeProvider.when('/bprocess/:BPid/perm/:id/show', {templateUrl: 'partials/forms/perms/perm-detail.html', controller: 'BPPermDetailCtrl'});
/*U*/$routeProvider.when('/bprocess/:BPid/perm/:id/edit', {templateUrl: 'partials/forms/perms/perm-edit.html', controller: 'BPPermDetailCtrl'});

    // Loggers
/*I*/$routeProvider.when('/bprocess/:BPid/logs', {templateUrl: 'partials/forms/bploggers/logger-list.html', controller: 'BPloggerListCtrl'});
/*R*/$routeProvider.when('/bprocess/:BPid/log/:id/show', {templateUrl: 'partials/forms/bploggers/logger-detail.html', controller: 'BPloggerDetailCtrl'});
/*C  $routeProvider.when('/bprocess/:BPid/log/new', {templateUrl: 'partials/bp_elements/bp-new.html', controller: 'BPCreationCtrl'}); */
/*U$routeProvider.when('/bprocess/:BPid/log/:id/edit', {templateUrl: 'partials/bp_elements/bp-edit.html', controller: 'BProcessDetailCtrl'});*/

     // Inputs
/*C*/$routeProvider.when('/bprocess/:BPid/input', {templateUrl: 'partials/forms/inputs/inputs.html', controller: 'BPRequestCtrl'});

     $routeProvider.otherwise({redirectTo: '/bprocesses'});



}]).run(['$window', '$rootScope', '$injector', function($window, $rootScope,$injector) {
    var token = $window.sessionStorage.getItem('token');
    $injector.get("$http").defaults.transformRequest = function(data, headersGetter) {
        //if ($rootScope.oauth)
        headersGetter()['X-Auth-Token'] = $window.sessionStorage.getItem('token');
        if (data) {
            return angular.toJson(data);
        }
    };
}]);




/* Filters

angular.module('myApp.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    }
  }]);
*/
