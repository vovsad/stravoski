/**
 * 
 */

var app = angular.module('StravoSki', ['ui.bootstrap']);

app.controller("AthleteCtrl", function($scope, $http) {
	  $http.get('/getathletestat').
	    success(function(data, status, headers, config) {
	      $scope.statistics = data;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
	});


app.controller("ActivitiesCtrl", function($scope, $http) {
  $http.get('/getactivities').
    success(function(data, status, headers, config) {
      $scope.activities = data;
    }).
    error(function(data, status, headers, config) {
    	$scope.message = "Something goes wrong";
    });
});

app.controller("TopCtrl", function($scope, $http, $modal, $log) {
	  
	//$scope.message = "Something goes wrong";
	
	  $scope.doSyncWithStrava = function () {
		  
	      	$http.get('/syncwithstrava').
		    success(function(data, status, headers, config) {
		    	$scope.messageTitle = 'Notification';
		    	$scope.messageBody = 'Your Activities are just synced and cached';
		    }).
		    error(function(data, status, headers, config) {
		    	$scope.messageTitle = 'Error';
		    	$scope.messageBody = 'Something goes wrong';
		    }).
		    then(function() {

		    	var modalInstance = $modal.open({
				      animation: $scope.animationsEnabled,
				      templateUrl: 'modalContent.html',
				      controller: 'ModalInstanceCtrl',
				      resolve: {
				        messageBody: function () {
				        	return $scope.messageBody;
				        },
				        messageTitle: function () {
				        	return $scope.messageTitle;
				        }

				      }
				    });

				    modalInstance.result.then(function (selectedItem) {
				      $scope.selected = selectedItem;
				    }, function () {
				      $log.info('Modal dismissed at: ' + new Date());
				    });
		    }
		    		);
	      	
		    

		    
		  };

});

app.controller('ModalInstanceCtrl', function ($scope, $modalInstance, messageBody, messageTitle) {
	$scope.messageBody = messageBody;
	$scope.messageTitle = messageTitle;
	  $scope.ok = function () {
	    $modalInstance.close('ok');
	  };
	});


