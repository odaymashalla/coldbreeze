/**
 * Common functionality.
 */

define(['angularAMD', 'angular',
	'app',
'pnotify',
//'lkgooglepicker',
'angular-animate',
'angular-websocket',
'toastr',
//'toastr-tpl',
'angular-pageslide-directive',
'jcs-auto-validate',
'angular-classy',
'angular-resource',
'angular-cookies',
'select',
'pnotifyconfirm',
'angularToasty',
'angularAudio',
'pnotifybuttons',
'angularpnotify',
//'filepicker',
//'angularfilepicker',
'notie',
'roundProgress',
'angular-moment',
'livestamp',
'ngDialog',
'cgNotify',
'ngFileUploadShim',
'ngFileUpload',
'angular-ui-tree',
'angular-underscore',
'loading-bar',
'ng-slide-down',
'ui-bootstrap',
'ui-bootstrap-tpls',
'toaster',
'angularLocalStorage',
'angular-awesome-error',
'angular-translate',
'angularDragAndDropLists',
'hljs',
'hljsangular',
'actionFormGenerator',
'actionFormDesctructor',

'restangular',
'ngsanitize',
'angular-cache',
'ui-select',
'angular-hovercard',
'angular-tooltips',
'apiCheck',
'angular-formly',
'angularformlybootstrap',
'daterangepicker',
'angular-daterangepicker',
'filters',
'services',
'launches',
'fastResourceCostCreation',
'treebuilder',
'controllers',
'actionController',
'launchDataController',
'procPermController',
'historyController',
'procController',
'procDetailController',
'procCreateController',
'requestController',
'loggerController',
'stationController',
'procElementController',
'launchedProcElementController',
'reflectionController',
'launchesController',
'directives',
'classie',
'cssParser'],
    function(angularAMD, angular, app, pnotify) {
  'use strict';
  console.log(angular);
  console.log(app);


  return angularAMD.bootstrap(app);
  //.module('yourprefix.common', ['common.helper', 'common.playRoutes', 'common.filters','common.directives.example']);
});
