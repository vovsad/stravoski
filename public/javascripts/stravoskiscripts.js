/**
 * 
 */

var app = angular.module('StravoSki', []);

app.controller("ActivitiesCtrl", function($scope, $http) {
  $http.get('/getactivities').
    success(function(data, status, headers, config) {
      $scope.activities = data;
    }).
    error(function(data, status, headers, config) {
      // log error
    });
});


/*
$http({ method: 'GET', url: '/syncwithstrava' }).
	  success(function (data, status, headers, config) {
	    alert(1);
	  }).
	  error(function (data, status, headers, config) {
		alert(2);
	  });
	return false;*/