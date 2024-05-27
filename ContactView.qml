import QtQuick

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
            inputField.text: contactModel ? contactModel.name : ""
            placeHolder : "Full name"

        }
        CustomTextInput {
            id: phoneNumber
            placeHolder  : "Phone number: +971.."
            inputField {
                text: contactModel ? contactModel.phoneNumber: ""
                validator: RegularExpressionValidator { regularExpression: /^[0-9+]*$/ }
                maximumLength: 13
            }
        }

        Row {
            anchors.horizontalCenter: parent.horizontalCenter
            spacing: 10
            Button {
                label.text: "Save"
                label.color: "white"
                background.color: "green"
                onClicked: {
                    if (fullName.inputField.text !== "" && phoneNumber.inputField.text !== ""){
                        if(contactModel) {
                            callBack(JSON.stringify({"name": fullName.inputField.text,  "phoneNumber": phoneNumber.inputField.text, "contactId": contactModel.contactId}), "edit")
                        }else {
                            callBack(JSON.stringify({"name": fullName.inputField.text,  "phoneNumber": phoneNumber.inputField.text, "contactId": "" }), "add")
                        }
                        contactRoot.destroy()

                        fullName.inputField.text = ""
                        phoneNumber.inputField.text = ""
                    }

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
