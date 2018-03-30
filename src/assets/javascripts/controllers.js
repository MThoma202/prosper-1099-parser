(function () {
    'use strict';
    var myApp = angular.module('app');
    myApp.controller('FileUploadController', function ($scope, fileUploadService) {

        $scope.uploadFile = function () {
            var file = $scope.myFile;
            var uploadUrl = "/convertPdfToCsv",
                promise = fileUploadService.uploadFileToUrl(file, uploadUrl);

            promise.then(function (response) {
                $scope.serverResponse = response;
            }, function () {
                $scope.serverResponse = 'An error has occurred.';
            })
        };
    });

})();
