$(window).load(function(){
	$(".player").droppable({ accept: '.player' });
	$(".player").draggable({ containment: "parent", revert: true });
});