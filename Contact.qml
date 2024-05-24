import QtQuick
// pragma NativeMethodBehavior: AcceptThisObject

Rectangle {
    id:contactRoot
    width: parent.width
    height: parent.height
    color: "#EBEDEF"
    property var contactModel: null
    property var callBack: ()=>{}
    MouseArea { anchors.fill: parent }
    clip: true

    Column {
        id:column
        anchors { fill: parent; margins: 20; topMargin: 150}
        spacing: 20
        CustomTextInput {
            id: fullName
            text: contactModel ? contactModel.name : ""
            placeHolder : "Full name"

        }
        CustomTextInput {
            id: phoneNumber
            text: contactModel ? contactModel.phoneNumber: ""
            placeHolder  : "Phone number"

        }

        Row {
            anchors.horizontalCenter: parent.horizontalCenter
            spacing: 10
            Button {
                label.text: "Save"
                label.color: "white"
                background.color: "green"
                onClicked: {
                    if (fullName.text !== "" && phoneNumber.text !== ""){
                        if(contactModel) {
                            callBack(fullName.text,  phoneNumber.text, contactModel.contactId, "edit")
                        }else {

                            callBack(fullName.text,  phoneNumber.text, "", "add")
                        }
                        contactRoot.destroy()

                    }
                    fullName.text = ""
                    phoneNumber.text = ""
                }
            }
            Button {
                label.text: "Cancel"
                label.color: "white"
                background.color: "red"
                onClicked: {
                    contactRoot.destroy()
                }
            }


        }

    }


    Header {
        title: contactModel ? "Edit Contact" : "Create Contact"
    }

}
