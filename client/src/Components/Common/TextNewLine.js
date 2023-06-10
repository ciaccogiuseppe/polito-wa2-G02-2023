function TextNewLine(text){
    return text.toString().split('\n').map(str => <p style={{marginTop:1, marginBottom:1}}>{str}<br/></p>)
}

export default TextNewLine