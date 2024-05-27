import QtQuick

MouseArea {
    width: label.width
    height: label.height
    property alias label: label
    property alias background: background
    Rectangle {
        id:background
        anchors {fill: parent; centerIn: parent }
        radius: 5
        color: "#EBEDEF"
    }
    Text {
        anchors.centerIn: parent
        id: label
        font.pixelSize: 16
        padding: 5
    }
}
