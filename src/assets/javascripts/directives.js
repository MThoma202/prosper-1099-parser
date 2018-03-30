(function () {
    'use strict';
    var myApp = angular.module('app');

    /*
     A directive to enable two way binding of file field
     */
    myApp.directive('fileModel', function ($parse) {
        return {
            restrict: 'A', // the directive can be used as an attribute only

            /*
             link is a function that defines functionality of a directive
             scope: the scope associated with the element
             element: the element on which this directive is used
             attrs: key-value pairs of element attributes
             */
            link: function (scope, element, attrs) {
                var model = $parse(attrs.fileModel),
                    modelSetter = model.assign; // define a setter for fileModel

                // Bind change event on the element
                element.bind('change', function () {
                    // Call apply on scope, it checks for value changes and reflect them on UI
                    scope.$apply(function () {
                        // set the model value
                        modelSetter(scope, element[0].files[0]);
                    });
                });
            }
        };
    });
})();
