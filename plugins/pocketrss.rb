# encoding: utf-8
module Jekyll

  class PocketRssListTag < Liquid::Tag
    require 'rss'
    require 'open-uri'

# ensure that pocket rss security is off (it requires a password)

    def render(context)
      html = "<ul>"
      url = "http://getpocket.com/users/chewmanfoo/feed/all"
     
      open(url) do |rss|
        feed = RSS::Parser.parse(rss)
        feed.items.each do |item|
          html << "<li><a href='#{item.link}'>#{item.title}</a></li>"
        end
        html << "</ul>"
      end
      html
    end

  end

end

# register the "pocketrss" tag
Liquid::Template.register_tag('pocketrss', Jekyll::PocketRssListTag)
