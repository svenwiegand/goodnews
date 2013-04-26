function fillValue(id, value) {
    document.getElementById(id).innerText = value;
}

function fillValues() {
    fillValue("activeArticleCount", statistics.formatActiveArticleCount());
    fillValue("inactiveArticleCount", statistics.formatInactiveArticleCount());
    fillValue("totalArticleCount", statistics.formatTotalArticleCount());

    fillValue("articleWithContentFileCount", statistics.formatArticleWithContentFileCount());
    fillValue("contentFileCount", statistics.formatContentFileCount());
    fillValue("articleWithImageFileCount", statistics.formatArticleWithImageFileCount());
    fillValue("imageFileCount", statistics.formatImageFileCount());
    fillValue("totalFileCount", statistics.formatTotalFileCount());

    fillValue("databaseSize", statistics.formatDatabaseSize());
    fillValue("contentFilesSize", statistics.formatContentFilesSize());
    fillValue("imageFilesSize", statistics.formatImageFilesSize());
    fillValue("totalFilesSize", statistics.formatTotalFilesSize());
}