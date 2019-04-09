import os
import json
import requests
import logging 
from telegram.ext import (Updater, CommandHandler, MessageHandler, Filters, RegexHandler, ConversationHandler)

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)

ENTER_HOUSE, ENTER_DANGER_LEVEL, ENTER_DESCRIPTION = range(3)

def cancel(update, context):
    update.message.reply_text("Your report has been discarded.")
    context.user_data.clear()
    return ConversationHandler.END

def start(update, context):
    update.message.reply_text("This is 911, what's the house you're at?")
    return ENTER_HOUSE

def entered_house(update, context):
    text = update.message.text
    context.user_data['house'] = text 
    update.message.reply_text(f"Reporting incident for house {text}. How would you assess the danger level?")
    return ENTER_DANGER_LEVEL

def entered_danger_level(update, context):
    text = update.message.text
    context.user_data['danger_level'] = text 
    update.message.reply_text(f"Assigned danger level is {text}. Any additional notes?")
    return ENTER_DESCRIPTION

def entered_description(update, context):
    text = update.message.text
    context.user_data['description'] = text 
    update.message.reply_text("Got your message. Stay safe!")
    send_report(context.user_data)
    context.user_data.clear()
    return ConversationHandler.END

def send_report(report):
    data_json = json.dumps(data)
    r = requests.post(REPORT_URL, data={
        'house': report['house'],
        'dangerLevel': report['danger_level'],
        'description': report['description']
    })

if __name__ == '__main__':
    bot_token = os.environ["BOT_TOKEN"]
    REPORT_URL = os.environ["REPORT_URL"]
    u = Updater(bot_token, use_context=True)

    u.dispatcher.add_handler(ConversationHandler(
        entry_points=[CommandHandler('start', start)],
        states={
            ENTER_HOUSE: [MessageHandler(Filters.text, entered_house, pass_user_data=True)],
            ENTER_DANGER_LEVEL: [MessageHandler(Filters.text, entered_danger_level, pass_user_data=True)],
            ENTER_DESCRIPTION: [MessageHandler(Filters.text, entered_description, pass_user_data=True)]
        },
        fallbacks=[CommandHandler('cancel', cancel)]
    ))
    u.start_polling()
