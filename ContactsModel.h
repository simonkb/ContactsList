#ifndef CONTACTSMODEL_H
#define CONTACTSMODEL_H
#include <QAbstractListModel>
#include <QList>
#include <QGuiApplication>
using namespace std;
#include <QJniObject>
#include <QObject>
#include <QJsonDocument>
#include <QJsonArray>
#include <QJsonObject>
#include <QString>
class ContactsModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum ContactRoles {
        NameRole = Qt::UserRole + 1,
        PhoneNumberRole,
        SelectedRole,
        ContactId,
    };

    ContactsModel(QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;

public slots:
    void addContact(const QVariantMap &contact);
    void updateContact(const QVariantMap &contact);
    void updateContacts(const QString &contacts);
    void deleteContacts(const QString &contacts);
    void deleteContact(const QVariantMap &contact);
    void loadDeviceContacts(const QString &contacts);
    bool setData(const QModelIndex &index, const QVariant &value, int role = Qt::EditRole) override;
    Q_INVOKABLE void onDeleteContacts();
    Q_INVOKABLE void onSaveContact(const QString &contact, const QString &action);
protected:
    QHash<int, QByteArray> roleNames() const override;

private:
    QList<QVariantMap> m_contacts;

signals:
    void rowsUpdated(int first, int last);
};


#endif // CONTACTSMODEL_H
