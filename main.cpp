#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QJniObject>
#include <ContactsModel.h>
#include <QQmlContext>

 int main(int argc, char *argv[])
{
    QGuiApplication app(argc, argv);
    QQmlApplicationEngine engine;
    qmlRegisterType<ContactsModel>("ContactsModel", 1, 0, "ContactsModel");
    const QUrl url(u"qrc:/ContactsListApp/Main.qml"_qs);
    QObject::connect(
        &engine,
        &QQmlApplicationEngine::objectCreationFailed,
        &app,
        []() { QCoreApplication::exit(-1); },
        Qt::QueuedConnection);
    engine.load(url);

    return app.exec();
}
