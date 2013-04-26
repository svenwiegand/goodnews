# GoodNews
GoodNews is an android client for Google Reader. It was originally a closed source commercial app, but due to Google's decision to shutdown Google Reader on July 1st I've decided to release it as open source. At the time of this writing GoodNews has a 4.5 star rating at Google play!

## What about podcasts?
The only feature which has been removed from the original app is the podcast functionality. I no longer think that integrating a news reader with a podcast player is a good idea. That's why I am working on my new podcast player called [uPod](http://upod.mobi) which will fully concentrate on podcasts and will be independent from Google Reader by bringing it's own server.

## And the former Pro Features?
Everything but the podcast functionality is available. This does also include former pro features like theming and fullscreen mode.

Stuff no longer required for an open source app (like e.g. advertisements, licensing) has also been removed, but everything else is there.

## What about code quality?
Well, that's a weak point. GoodNews has been developed completely in my free time and as I've never planned to make it open source it is completely missing things like code documentation and unit tests. I've had a bunch of integration test, mainly to test the sync functionality, but they were based on my private Google Reader account and so I needed to dismiss them before going open source. If one of my colleagues would deliver code with this quality, I would kill him/her ;-)

The coding style is based on the original java coding style with a few minor adjustments (two spaces for indentation instead of four and a max line length of 120 characters).

You should also know, that GoodNews has been developed using IntelliJ IDEA -- an excellent IDE and the community edition is also available for free.

## What do you plan with the project?
I no longer have time to invest coding time into GoodNews, but I would be willing to keep the app alive under it's original GoodNews brand if other developers care about further development. I would then create new releases and provide them at Google play! -- naturally the paid license will no longer be required -- GoodNews will be completely free.

## What are the most required features?
1. **Support for Feedly:** Google Reader will be shut down on July 1st and so the first thing that needs to be done is to migrate GoodNews to another backend. Feedly promised to provide a compatible API,so migrating shouldn't be too hard (though I am sure that it will not work out of the box).
2. **Tablet support aka responsive design:** This is a really huge issue. GoodNews has been implemented before the fragment API has been introduced with Android 3. Thus this will require huge parts of the user interface to be restructured.
3. **Teaser Images:** To keep up with other news readers, GoodNews will need teaser images in the image list.

## How can I participate?
Simply go the GitHub way:
1. fork
2. implement
3. create pull request
