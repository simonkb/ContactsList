#include "ContactsModel.h"
#include <jni.h>
#include <QMetaObject>

extern "C" JNIEXPORT void JNICALL Java_com_example_contactslist_MainActivity_onContactsLoaded(JNIEnv *env, jobject obj, jstring str, jlong ptr) {
    QString contacts = env->GetStringUTFChars(str, nullptr);
    ContactsModel *contactsModel = (ContactsModel*)ptr;
    if (contactsModel)
        QMetaObject::invokeMethod(contactsModel, "loadDeviceContacts", Qt::QueuedConnection, Q_ARG(QString, contacts));
}

extern "C" JNIEXPORT void JNICALL Java_com_example_contactslist_MainActivity_onContactsChanged(JNIEnv *env, jobject obj, jstring str, jlong ptr, jstring action) {
    QString contacts = env->GetStringUTFChars(str, nullptr);
    QString actionStr = env->GetStringUTFChars(action, nullptr);
    ContactsModel *contactsModel = (ContactsModel*)ptr;
    if (contactsModel)
        QMetaObject::invokeMethod(contactsModel, "modifyContacts", Qt::QueuedConnection, Q_ARG(QString, contacts), Q_ARG(QString, actionStr));
}

ContactsModel::ContactsModel(QObject *parent)
    : QAbstractListModel(parent) {
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("fetchContacts", "(J)V", (long long)this);
}
bool compareMaps(const QVariantMap &map1, const QVariantMap &map2) {
    QString name1 = map1["name"].toString();
    QString name2 = map2["name"].toString();
    return name1 < name2;
}

void sortVariantMapList(QList<QVariantMap> &list) {
    std::sort(list.begin(), list.end(), compareMaps);
}

void ContactsModel::loadDeviceContacts(const QString &jsonString) {
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    m_contacts.clear();
    for(QVariant contact: list ){
        addContact(contact.toMap());
    }
    sortVariantMapList(m_contacts);
    emit dataChanged(index(0), index(m_contacts.size()-1));
}

void ContactsModel::modifyContacts(const QString &jsonString, const QString &action) {
    if(action == "deleted")
        deleteContacts(jsonString);
    else
        updateContacts(jsonString);
}

void ContactsModel::addContact(const QVariantMap &contact) {
    beginInsertRows(QModelIndex(), rowCount(), rowCount());
    m_contacts.append(contact);
    endInsertRows();
}

void ContactsModel::deleteContact(const QVariantMap &contact){
    for (int i= 0; i<m_contacts.size(); ++i) {
        if (m_contacts.at(i).value("contactId").toInt() == contact.value("contactId").toInt() ) {
            beginRemoveRows(QModelIndex(), i, i);
            m_contacts.removeAt(i);
            endRemoveRows();
        }
    }
}

void ContactsModel :: deleteContacts(const QString &jsonString){
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    for(QVariant contact: list ){
        deleteContact(contact.toMap());
    }
}

void ContactsModel::updateContact(const QVariantMap &contact){
    for (int i= 0; i<m_contacts.size(); ++i) {
        if (m_contacts.at(i).value("contactId").toInt() == contact.value("contactId").toInt() ) {
                m_contacts[i] = contact;
                emit dataChanged(index(i), index(i));
                return;
        }
    }
    beginInsertColumns(QModelIndex(), rowCount()+1, rowCount()+1);
    m_contacts.append(contact);
    endInsertRows();
}
int ContactsModel::rowCount(const QModelIndex &parent) const {
    Q_UNUSED(parent);
    return m_contacts.size();
}

QVariant ContactsModel::data(const QModelIndex &index, int role) const {
    if (index.row() < 0 || index.row() >= m_contacts.size())
        return QVariant();

    const QVariantMap &contact = m_contacts.at(index.row());
    switch (role) {
    case NameRole:
        return contact["name"];
    case PhoneNumberRole:
        return contact["phoneNumber"];
    case SelectedRole:
        return contact["selected"];
    case ContactId :
        return contact["contactId"];
    default:
        return QVariant();
    }
}

QHash<int, QByteArray> ContactsModel::roleNames() const {
    QHash<int, QByteArray> roles;
    roles[NameRole] = "name";
    roles[PhoneNumberRole] = "phoneNumber";
    roles[SelectedRole] = "selected";
    roles[ContactId] = "contactId";
    return roles;
}
void ContactsModel :: updateContacts (const QString &jsonString){
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    for(QVariant contact: list ){
        updateContact(contact.toMap());
    }
    sortVariantMapList(m_contacts);
    emit dataChanged(index(0), index(m_contacts.size()-1));
}

bool ContactsModel::setData (const QModelIndex &index, const QVariant &value, int role) {
    if (!index.isValid() || index.row() >= m_contacts.size())
        return false;
    QVariantMap &contact = m_contacts[index.row()];
    bool changed = false;
    switch (role) {
    case NameRole:
        if (contact["name"] != value.toString()) {
            contact["name"] = value.toString();
            changed = true;
        }
        break;
    case PhoneNumberRole:
        if (contact["phone"] != value.toString()) {
            contact["phone"] = value.toString();
            changed = true;
        }
        break;
    case SelectedRole:
        contact["selected"] = value.toBool();
        changed = true;
        break;
    }

    if (changed) {
        emit dataChanged(index, index, QVector<int>() << role);
        return true;
    }
    return false;
}
void ContactsModel::onDeleteContacts() {
    QStringList ids;
    for (const QVariantMap &map : m_contacts) {
        if(map["selected"].toBool())
            ids.append(map.value("contactId").toString());
    }
    QString idsString = ids.join(",");
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("deleteContacts", "(Ljava/lang/String;)V", QJniObject::fromString(idsString).object<jstring>());
}
void ContactsModel::onSaveContact (const QString &contactJson, const QString &action) {
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("saveContact", "(Ljava/lang/String;Ljava/lang/String;)V", QJniObject::fromString(contactJson).object<jstring>(), QJniObject::fromString(action).object<jstring>());
}

