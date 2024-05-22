import QtQuick
Rectangle {
    anchors.horizontalCenter: parent.horizontalCenter
    width: parent.width * 0.9
    height: input.height
    radius: 5
    property alias text:  input.text
    property alias placeHolder: pHolder.text
    TextEdit {
        id: input
        padding: 10
        width: parent.width
        verticalAlignment: Text.AlignVCenter
        wrapMode: Text.Wrap
        font.pixelSize: 14

        onTextChanged: {
            let max = 51
            if (text.length > max) {
                text = text.substring (0, max)
                parent.border.color = "red"
            } else if (text.length == 0) {
                parent.border.color = "red"
            } else {
                parent.border.color = "white"
            }
        }
        Text {
              id: pHolder
              verticalAlignment: Text.AlignVCenter
              visible: parent.text === "" && !parent.activeFocus
              color: 'gray'
              padding: 10
          }
    }

}

