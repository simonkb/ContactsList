import QtQuick

Rectangle {
    id: header
    width: parent.width;
    height: 70
    anchors.top: parent.top
    color: "#EBEDEF"
    property alias title: headerTitle.text
    property alias addOrSave: addOrSaveBtn
    property alias searchOrcancel: searchOrCancelBtn

    Text {
        id: headerTitle
        anchors {verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 30}
        text: "Contacts"
        font.pixelSize: 18
    }
    Row {
        anchors {verticalCenter: parent.verticalCenter; right: parent.right; rightMargin: 20}
        spacing: 10
        Button {
            id: addOrSaveBtn
            visible: false
        }
        Button {
            id: searchOrCancelBtn
            visible: false
        }
    }
}
