function extractAnswerFromArray(array){
    console.log(array);

    var string = "";

    if (array.length !== 0) {
        for (var i = 0; i < array.length; i++){
            string.concat(array[i]);
            if (i === array.length - 1) break;
            else string.concat(", ");
        }
    }

    console.log(string);
    document.querySelector(".correct-answer").textContent="" + string;


}
