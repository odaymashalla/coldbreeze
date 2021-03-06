'use strict';

/* Directives 


angular.module('myApp.directives', []).
  directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
      elm.text(version);
    };
  }]); */
define(['angular', 'app'], function (angular, minorityApp) {

minorityApp.directive('editInPlace', function () {
    return {
        restrict: 'E',
        scope: {
            value: '='
        },
        template: '<span ng-click="edit()" class="edit" ng-bind="value"></span><input ng-model="value"></input><span class="ok" ng-click="ok()">Ok</span><span class="cancel" ng-click="cancel()">Cancel</span>',
        link: function ($scope, element, attrs) {
            // Let's get a reference to the input element, as we'll want to reference it.
            var inputElement = angular.element(element.children()[1]);

            // This directive should have a set class so we can style it.
            element.addClass('edit-in-place');

            // Initially, we're not editing.
            $scope.editing = false;

            // ng-click handler to activate edit-in-place
            $scope.edit = function () {
                $scope.editing = true;
                $scope.old_value = $scope.value;

                // We control display through a class on the directive itself. See the CSS.
                element.addClass('active');

                // And we must focus the element. 
                // `angular.element()` provides a chainable array, like jQuery so to access a native DOM function, 
                // we have to reference the first element in the array.
                inputElement[0].focus();
            };
            $scope.ok = function () {
                $scope.editing = false;
                $scope.$parent.updateInline($scope.value);
                element.removeClass('active');
            };
            $scope.cancel = function () {
                $scope.editing = false;
                $scope.value = $scope.old_value;
                element.removeClass('active');
            };

            // When we leave the input, we're done editing.
            inputElement.prop('onblur', function () {
                $scope.editing = false;
                element.removeClass('active');
            });
        }
    };
});

return minorityApp;

});