import os
import os.path

import numpy as np
from keras.models import Model, load_model
from keras.layers import Input, Dense, concatenate
from keras.optimizers import SGD
from keras.utils import plot_model

####################################################
#--------------------SUB_NETS----------------------#
####################################################


def buildAggrPass(tname):
    aggr_pass_input = Input(shape=(5,), name=tname)
    x = Dense(4, activation='relu')(aggr_pass_input)
    x = Dense(4, activation='relu')(x)
    aggr_pass_output = Dense(2, activation='sigmoid')(x)
    return (aggr_pass_input, aggr_pass_output)


def buildLooseTight(tname):
    loose_tight_input = Input(shape=(5,), name=tname)
    x = Dense(4, activation='relu')(loose_tight_input)
    x = Dense(4, activation='relu')(x)
    loose_tight_output = Dense(2, activation='sigmoid')(x)
    return (loose_tight_input, loose_tight_output)


def buildHoleCards():
    hole_cards_input = Input(shape=(4,), name='hole_cards')
    x = Dense(4, activation='relu')(hole_cards_input)
    hole_cards_output = Dense(3, activation='sigmoid')(x)
    return (hole_cards_input, hole_cards_output)


def buildAction():
    action_input = Input(shape=(3,), name='action')
    x = Dense(2, activation='relu')(action_input)
    action_output = Dense(1, activation='sigmoid')(x)
    return (action_input, action_output)


def buildBettingRound(tname):
    if tname is "preflop":
        betting_round_input = Input(shape=(6,), name=tname)
    else:
        betting_round_input = Input(shape=(7,), name=tname)

    x = Dense(8, activation='relu')(betting_round_input)
    x = Dense(8, activation='relu')(x)
    betting_round_output = Dense(4, activation='sigmoid')(x)
    return (betting_round_input, betting_round_output)


def buildHand():
    preflop = buildBettingRound('preflop')
    flop = buildBettingRound('flop')
    turn = buildBettingRound('turn')
    river = buildBettingRound('river')

    hand_input = concatenate([preflop[1], flop[1], turn[1], river[1]])
    x = Dense(24, activation='relu')(hand_input)
    x = Dense(16, activation='relu')(x)
    hand_output = Dense(8, activation='sigmoid')(x)
    return ([preflop[0], flop[0], turn[0], river[0]], hand_output)


def buildBoard():
    board_input = Input(shape=(10,), name='board')
    x = Dense(8, activation='relu')(board_input)
    x = Dense(4, activation='relu')(x)
    board_output = Dense(3, activation='sigmoid')(x)
    return (board_input, board_output)


def buildHandOutcome():
    hand_outcome_input = Input(shape=(16,), name='hand_outcome')
    x = Dense(12, activation='relu')(hand_outcome_input)
    x = Dense(12, activation='relu')(x)
    hand_outcome_output = Dense(8, activation='sigmoid')(x)
    return (hand_outcome_input, hand_outcome_output)


def buildOuts():
    outs_input = Input(shape=(27,), name='outs')
    x = Dense(32, activation='relu')(outs_input)
    x = Dense(32, activation='relu')(x)
    outs_output = Dense(16, activation='sigmoid')(x)
    return (outs_input, outs_output)


def buildRangeDown(tname):
    range_down_input = Input(shape=(37,), name=tname)
    x = Dense(32, activation='relu')(range_down_input)
    x = Dense(16, activation='relu')(x)
    range_down_output = Dense(8, activation='sigmoid')(x)
    return (range_down_input, range_down_output)

####################################################
#--------------------MAIN_NETS---------------------#
####################################################


def buildPreflopRange():
    single_data = Input(shape=(8,), dtype='float32', name='single_data')
    aggr_pass = buildAggrPass('aggr_pass')
    loose_tight = buildLooseTight('loose_tight')
    preflop = buildBettingRound('preflop')

    preflop_range_input = concatenate(
        [single_data, aggr_pass[1], loose_tight[1], preflop[1]])
    x = Dense(64, activation='relu')(preflop_range_input)
    x = Dense(64, activation='relu')(x)
    preflop_range_output = Dense(37, activation='sigmoid')(x)

    preflop_range = Model(inputs=[single_data, aggr_pass[0], loose_tight[0], preflop[0]],
                          outputs=preflop_range_output)
    return preflop_range


def buildPostflopRange():
    single_data = Input(shape=(10,), dtype='float32', name='single_data')
    aggr_pass = buildAggrPass('aggr_pass')
    loose_tight = buildLooseTight('loose_tight')
    hand = buildHand()
    board = buildBoard()

    postflop_range_input = concatenate(
        [single_data, aggr_pass[1], loose_tight[1], hand[1], board[1]])
    x = Dense(64, activation='relu')(postflop_range_input)
    x = Dense(64, activation='relu')(x)
    postflop_range_output = Dense(37, activation='sigmoid')(x)

    postflop_range = Model(inputs=[single_data, aggr_pass[0], loose_tight[0],
                                   hand[0][0], hand[0][1], hand[0][2], hand[0][3], board[0]],
                           outputs=postflop_range_output)
    return postflop_range


def buildPreflopAction():
    single_data = Input(shape=(13,), name='single_data')
    aggr_pass_me = buildAggrPass('aggr_pass_me')
    loose_tight_me = buildLooseTight('looose_tight_me')
    aggr_pass_op = buildAggrPass('aggr_pass_op')
    loose_tight_op = buildLooseTight('loose_tight_op')
    hole_cards = buildHoleCards()
    latest_action = buildAction()
    preflop = buildBettingRound('preflop')
    range_preflop_me = buildRangeDown('range_down_preflop_me')
    range_preflop_op = buildRangeDown('range_down_preflop_op')

    preflop_action_input = concatenate([single_data,  aggr_pass_me[1], loose_tight_me[1], aggr_pass_op[1], loose_tight_op[1],
                                        hole_cards[1], latest_action[1], preflop[1], range_preflop_me[1], range_preflop_op[1]])
    x = Dense(32, activation='relu')(preflop_action_input)
    x = Dense(32, activation='relu')(x)
    x = Dense(16, activation='relu')(x)
    preflop_action_output = Dense(3, activation='sigmoid')(x)

    preflop_action = Model(inputs=[single_data, aggr_pass_me[0], loose_tight_me[0], aggr_pass_op[0], loose_tight_op[0],
                                   hole_cards[0], latest_action[0], preflop[0], range_preflop_me[0], range_preflop_op[0]],
                           outputs=preflop_action_output)
    return preflop_action


def buildPreflopAmount():
    single_data = Input(shape=(13,), name='single_data')
    aggr_pass_me = buildAggrPass('aggr_pass_me')
    loose_tight_me = buildLooseTight('looose_tight_me')
    aggr_pass_op = buildAggrPass('aggr_pass_op')
    loose_tight_op = buildLooseTight('loose_tight_op')
    hole_cards = buildHoleCards()
    latest_action = buildAction()
    preflop = buildBettingRound('preflop')
    range_preflop_me = buildRangeDown('range_down_preflop_me')
    range_preflop_op = buildRangeDown('range_down_preflop_op')

    preflop_amount_input = concatenate([single_data,  aggr_pass_me[1], loose_tight_me[1], aggr_pass_op[1], loose_tight_op[1],
                                        hole_cards[1], latest_action[1], preflop[1], range_preflop_me[1], range_preflop_op[1]])
    x = Dense(64, activation='relu')(preflop_amount_input)
    x = Dense(64, activation='relu')(x)
    preflop_amount_output = Dense(32, activation='sigmoid')(x)

    preflop_amount = Model(inputs=[single_data, aggr_pass_me[0], loose_tight_me[0], aggr_pass_op[0], loose_tight_op[0],
                                   hole_cards[0], latest_action[0], preflop[0], range_preflop_me[0], range_preflop_op[0]],
                           outputs=preflop_amount_output)
    return preflop_amount


def buildPostflopAction():
    single_data = Input(shape=(15,), name='single_data')
    aggr_pass_me = buildAggrPass('aggr_pass_me')
    loose_tight_me = buildLooseTight('looose_tight_me')
    aggr_pass_op = buildAggrPass('aggr_pass_op')
    loose_tight_op = buildLooseTight('loose_tight_op')
    hole_cards = buildHoleCards()
    latest_action = buildAction()
    board = buildBoard()
    hand = buildHand()
    hand_outcome = buildHandOutcome()
    outs = buildOuts()
    range_postflop_me = buildRangeDown('range_down_postflop_me')
    range_postflop_op = buildRangeDown('range_down_postflop_op')

    postflop_action_input = concatenate([single_data, aggr_pass_me[1], loose_tight_me[1], aggr_pass_op[1], loose_tight_op[1],
                                         hole_cards[1], latest_action[1], hand[1],  board[1], hand_outcome[1], outs[1],
                                         range_postflop_me[1], range_postflop_op[1]])
    x = Dense(64, activation='relu')(postflop_action_input)
    x = Dense(64, activation='relu')(x)
    x = Dense(32, activation='relu')(x)
    postflop_action_output = Dense(3, activation='sigmoid')(x)

    postflop_action = Model(inputs=[single_data, aggr_pass_me[0], loose_tight_me[0], aggr_pass_op[0], loose_tight_op[0],
                                    hole_cards[0], latest_action[0], hand[0][0], hand[0][1], hand[0][2], hand[0][3],
                                    board[0], hand_outcome[0], outs[0], range_postflop_me[0], range_postflop_op[0]],
                            outputs=postflop_action_output)
    return postflop_action


def buildPostflopAmount():
    single_data = Input(shape=(15,), name='single_data')
    aggr_pass_me = buildAggrPass('aggr_pass_me')
    loose_tight_me = buildLooseTight('looose_tight_me')
    aggr_pass_op = buildAggrPass('aggr_pass_op')
    loose_tight_op = buildLooseTight('loose_tight_op')
    hole_cards = buildHoleCards()
    latest_action = buildAction()
    board = buildBoard()
    hand = buildHand()
    hand_outcome = buildHandOutcome()
    outs = buildOuts()
    range_postflop_me = buildRangeDown('range_down_postflop_me')
    range_postflop_op = buildRangeDown('range_down_postflop_op')

    postflop_amount_input = concatenate([single_data, aggr_pass_me[1], loose_tight_me[1], aggr_pass_op[1], loose_tight_op[1],
                                         hole_cards[1], latest_action[1], hand[1],  board[1], hand_outcome[1], outs[1],
                                         range_postflop_me[1], range_postflop_op[1]])
    x = Dense(64, activation='relu')(postflop_amount_input)
    x = Dense(64, activation='relu')(x)
    x = Dense(32, activation='relu')(x)
    postflop_amount_output = Dense(32, activation='sigmoid')(x)

    postflop_amount = Model(inputs=[single_data, aggr_pass_me[0], loose_tight_me[0], aggr_pass_op[0], loose_tight_op[0],
                                    hole_cards[0], latest_action[0], hand[0][0], hand[0][1], hand[0][2], hand[0][3],
                                    board[0], hand_outcome[0], outs[0], range_postflop_me[0], range_postflop_op[0]],
                            outputs=postflop_amount_output)
    return postflop_amount

####################################################
#--------------------TRAINING----------------------#
####################################################


input_path = "/Users/linusstenzel/Desktop/poker_ai/ai/training_data/input/"
output_path = "/Users/linusstenzel/Desktop/poker_ai/ai/training_data/output/"
model_path = "/Users/linusstenzel/Desktop/poker_ai/ai/models/"
pred_truth_path = "/Users/linusstenzel/Desktop/poker_ai/ai/pred_truth/"
prediction_count = 20


def trainPreflopRange():
    print("Training Pre Range")
    pre_ra_count = fileLen(input_path + "rangePre.csv")

    x_single_data = np.zeros(shape=(pre_ra_count, 8))
    x_aggr_pass = np.zeros(shape=(pre_ra_count, 5))
    x_loose_tight = np.zeros(shape=(pre_ra_count, 5))
    x_preflop = np.zeros(shape=(pre_ra_count, 6))
    y = np.zeros(shape=(pre_ra_count, 37))

    i = 0
    with open(input_path + "rangePre.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            x_single_data[i] = input_raw[:8]
            x_aggr_pass[i] = input_raw[8:13]
            x_loose_tight[i] = input_raw[13:18]
            x_preflop[i] = input_raw[18:]
            i += 1
    i = 0
    with open(output_path + "rangePre.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    preflop_range = buildPreflopRange()
    vzl(preflop_range, "preflop_range.png")

    preflop_range.compile(optimizer=SGD(lr=0.0001),
                          loss='binary_crossentropy',
                          metrics=['accuracy'])
    preflop_range.fit([x_single_data, x_aggr_pass, x_loose_tight, x_preflop],
                      y, epochs=80, batch_size=64)

    preflop_range.save(model_path + "preflop_range.h5")

    predictions = preflop_range.predict(
        [x_single_data[:200:10], x_aggr_pass[:200:10], x_loose_tight[:200:10], x_preflop[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "rangePre.csv", 37)


def trainPostflopRange():
    print("Training Post Range")
    post_ra_count = fileLen(input_path + "rangePost.csv")

    x_single_data = np.zeros(shape=(post_ra_count, 10))
    x_aggr_pass = np.zeros(shape=(post_ra_count, 5))
    x_loose_tight = np.zeros(shape=(post_ra_count, 5))
    x_preflop = np.zeros(shape=(post_ra_count, 6))
    x_flop = np.zeros(shape=(post_ra_count, 7))
    x_turn = np.zeros(shape=(post_ra_count, 7))
    x_river = np.zeros(shape=(post_ra_count, 7))
    x_board = np.zeros(shape=(post_ra_count, 10))
    y = np.zeros(shape=(post_ra_count, 37))

    i = 0
    with open(input_path + "rangePost.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            x_single_data[i] = input_raw[:10]
            x_aggr_pass[i] = input_raw[10:15]
            x_loose_tight[i] = input_raw[15:20]
            x_preflop[i] = input_raw[20:26]
            x_flop[i] = input_raw[26:33]
            x_turn[i] = input_raw[33:40]
            x_river[i] = input_raw[40:47]
            x_board[i] = input_raw[47:]
            i += 1
    i = 0
    with open(output_path + "rangePost.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    postflop_range = buildPostflopRange()
    vzl(postflop_range, "postflop_range.png")

    postflop_range.compile(optimizer=SGD(lr=0.0001),
                           loss='binary_crossentropy',
                           metrics=['accuracy'])
    postflop_range.fit([x_single_data, x_aggr_pass, x_loose_tight, x_preflop, x_flop, x_turn, x_river, x_board],
                       y, epochs=80, batch_size=64)

    postflop_range.save(model_path + "postflop_range.h5")

    predictions = postflop_range.predict([x_single_data[:200:10], x_aggr_pass[:200:10], x_loose_tight[:200:10],
                                          x_preflop[:200:10], x_flop[:200:10], x_turn[:200:10], x_river[:200:10], x_board[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "rangePost.csv", 37)


def trainPreflopAction():
    print("Training Pre Action")
    pre_ac_count = fileLen(input_path + "actionPre.csv")

    x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop = initXpre(
        pre_ac_count)
    y = np.zeros(shape=(pre_ac_count, 3))

    i = 0
    with open(input_path + "actionPre.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            fillXpre(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop,
                     x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop,
                     x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop)
            i += 1
    i = 0
    with open(output_path + "actionPre.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    preflop_range = load_model(model_path + "preflop_range.h5")
    x_range_me_down = preflop_range.predict(
        [x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop])
    x_range_op_down = preflop_range.predict(
        [x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop])

    preflop_action = buildPreflopAction()
    vzl(preflop_action, "preflop_action.png")

    preflop_action.compile(optimizer=SGD(lr=0.0001),
                           loss='binary_crossentropy',
                           metrics=['accuracy'])

    preflop_action.fit([x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op,
                        x_hole_cards, x_latest_action, x_preflop, x_range_me_down, x_range_op_down],
                       y, epochs=80, batch_size=32)

    preflop_action.save(model_path + "preflop_action.h5")

    predictions = preflop_action.predict([x_single_data[:200:10], x_aggr_pass_me[:200:10], x_loose_tight_me[:200:10], x_aggr_pass_op[:200:10], x_loose_tight_op[:200:10],
                                          x_hole_cards[:200:10], x_latest_action[:200:10], x_preflop[:200:10], x_range_me_down[:200:10], x_range_op_down[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "actionPre.csv", 3)


def trainPreflopAmount():
    print("Training Pre Amount")
    pre_am_count = fileLen(input_path + "amountPre.csv")

    x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop = initXpre(
        pre_am_count)
    y = np.zeros(shape=(pre_am_count, 32))

    i = 0
    with open(input_path + "amountPre.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            fillXpre(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop,
                     x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop,
                     x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop)
            i += 1
    i = 0
    with open(output_path + "amountPre.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    preflop_range = load_model(model_path + "preflop_range.h5")
    x_range_me_down = preflop_range.predict(
        [x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop])
    x_range_op_down = preflop_range.predict(
        [x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop])

    preflop_amount = buildPreflopAmount()
    vzl(preflop_amount, "preflop_amount.png")

    preflop_amount.compile(optimizer=SGD(lr=0.0001),
                           loss='binary_crossentropy',
                           metrics=['accuracy'])
    preflop_amount.fit([x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op,
                        x_hole_cards, x_latest_action, x_preflop, x_range_me_down, x_range_op_down],
                       y, epochs=80, batch_size=32)

    preflop_amount.save(model_path + "preflop_amount.h5")

    predictions = preflop_amount.predict([x_single_data[:200:10], x_aggr_pass_me[:200:10], x_loose_tight_me[:200:10], x_aggr_pass_op[:200:10], x_loose_tight_op[:200:10],
                                          x_hole_cards[:200:10], x_latest_action[:200:10], x_preflop[:200:10], x_range_me_down[:200:10], x_range_op_down[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "amountPre.csv", 32)


def initXpre(count):
    x_single_data = np.zeros(shape=(count, 13))
    x_aggr_pass_me = np.zeros(shape=(count, 5))
    x_loose_tight_me = np.zeros(shape=(count, 5))
    x_aggr_pass_op = np.zeros(shape=(count, 5))
    x_loose_tight_op = np.zeros(shape=(count, 5))
    x_hole_cards = np.zeros(shape=(count, 4))
    x_latest_action = np.zeros(shape=(count, 3))
    x_preflop = np.zeros(shape=(count, 6))

    x_range_me_single_data = np.zeros(shape=(count, 8))
    x_range_me_aggr_pass = np.zeros(shape=(count, 5))
    x_range_me_loose_tight = np.zeros(shape=(count, 5))
    x_range_me_preflop = np.zeros(shape=(count, 6))

    x_range_op_single_data = np.zeros(shape=(count, 8))
    x_range_op_aggr_pass = np.zeros(shape=(count, 5))
    x_range_op_loose_tight = np.zeros(shape=(count, 5))
    x_range_op_preflop = np.zeros(shape=(count, 6))
    return x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop


def fillXpre(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop):
    x_single_data[i] = input_raw[:13]
    x_aggr_pass_me[i] = input_raw[13:18]
    x_loose_tight_me[i] = input_raw[18:23]
    x_aggr_pass_op[i] = input_raw[23:28]
    x_loose_tight_op[i] = input_raw[28:33]
    x_hole_cards[i] = input_raw[33:37]
    x_latest_action[i] = input_raw[37:40]
    x_preflop[i] = input_raw[40:46]

    x_range_me_single_data[i] = input_raw[46:54]
    x_range_me_aggr_pass[i] = input_raw[54:59]
    x_range_me_loose_tight[i] = input_raw[59:64]
    x_range_me_preflop[i] = input_raw[64:70]

    x_range_op_single_data[i] = input_raw[70:78]
    x_range_op_aggr_pass[i] = input_raw[78:83]
    x_range_op_loose_tight[i] = input_raw[83:88]
    x_range_op_preflop[i] = input_raw[88:]


def trainPostflopAction():
    print("Training Post Action")

    post_ac_count = fileLen(input_path + "actionPost.csv")

    x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board = initXpost(
        post_ac_count)
    y = np.zeros(shape=(post_ac_count, 3))

    i = 0
    with open(input_path + "actionPost.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            fillXpost(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs,
                      x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board,
                      x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board)
            i += 1
    i = 0
    with open(output_path + "actionPost.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    postflop_range = load_model(model_path + "postflop_range.h5")

    x_range_me_down = postflop_range.predict(
        [x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight,
         x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board])

    x_range_op_down = postflop_range.predict(
        [x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight,
         x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board])

    postflop_action = buildPostflopAction()
    vzl(postflop_action, "postflop_action.png")

    postflop_action.compile(optimizer=SGD(lr=0.0001),
                            loss='binary_crossentropy',
                            metrics=['accuracy'])
    postflop_action.fit([x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op,
                         x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs,
                         x_range_me_down, x_range_op_down],
                        y, epochs=80, batch_size=32)

    postflop_action.save(model_path + "postflop_action.h5")

    predictions = postflop_action.predict([x_single_data[:200:10], x_aggr_pass_me[:200:10], x_loose_tight_me[:200:10], x_aggr_pass_op[:200:10], x_loose_tight_op[:200:10],
                                           x_hole_cards[:200:10], x_latest_action[:200:10],
                                           x_preflop[:200:10], x_flop[:200:10], x_turn[:200:10], x_river[:200:10],
                                           x_board[:200:10], x_hand_outcome[:200:10], x_outs[:200:10], x_range_me_down[:200:10], x_range_op_down[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "actionPost.csv", 3)


def trainPostflopAmount():
    print("Training Post Amount")
    post_am_count = fileLen(input_path + "amountPost.csv")

    x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board = initXpost(
        post_am_count)
    y = np.zeros(shape=(post_am_count, 32))

    i = 0
    with open(input_path + "amountPost.csv", "r") as f:
        for x in f:
            input_raw = np.fromstring(x, sep=",")
            fillXpost(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs,
                      x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board,
                      x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board)
            i += 1
    i = 0
    with open(output_path + "amountPost.csv", "r") as f:
        for x in f:
            y[i] = np.fromstring(x, sep=",")
            i += 1

    postflop_range = load_model(model_path + "postflop_range.h5")

    x_range_me_down = postflop_range.predict(
        [x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight,
         x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board])

    x_range_op_down = postflop_range.predict(
        [x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight,
         x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board])

    postflop_amount = buildPostflopAmount()
    vzl(postflop_amount, "postflop_amount.png")

    postflop_amount.compile(optimizer=SGD(lr=0.0001),
                            loss='binary_crossentropy',
                            metrics=['accuracy'])
    postflop_amount.fit([x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op,
                         x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs,
                         x_range_me_down, x_range_op_down],
                        y, epochs=80, batch_size=32)

    postflop_amount.save(model_path + "postflop_amount.h5")

    predictions = postflop_amount.predict([x_single_data[:200:10], x_aggr_pass_me[:200:10], x_loose_tight_me[:200:10], x_aggr_pass_op[:200:10], x_loose_tight_op[:200:10],
                                           x_hole_cards[:200:10], x_latest_action[:200:10],
                                           x_preflop[:200:10], x_flop[:200:10], x_turn[:200:10], x_river[:200:10],
                                           x_board[:200:10], x_hand_outcome[:200:10], x_outs[:200:10], x_range_me_down[:200:10], x_range_op_down[:200:10]])
    truth = y[:200:10]
    savePredTruth(predictions, truth, "amountPost.csv", 32)


def initXpost(count):
    x_single_data = np.zeros(shape=(count, 15))
    x_aggr_pass_me = np.zeros(shape=(count, 5))
    x_loose_tight_me = np.zeros(shape=(count, 5))
    x_aggr_pass_op = np.zeros(shape=(count, 5))
    x_loose_tight_op = np.zeros(shape=(count, 5))
    x_hole_cards = np.zeros(shape=(count, 4))
    x_latest_action = np.zeros(shape=(count, 3))
    x_preflop = np.zeros(shape=(count, 6))
    x_flop = np.zeros(shape=(count, 7))
    x_turn = np.zeros(shape=(count, 7))
    x_river = np.zeros(shape=(count, 7))
    x_board = np.zeros(shape=(count, 10))
    x_hand_outcome = np.zeros(shape=(count, 16))
    x_outs = np.zeros(shape=(count, 27))

    x_range_me_single_data = np.zeros(shape=(count, 10))
    x_range_me_aggr_pass = np.zeros(shape=(count, 5))
    x_range_me_loose_tight = np.zeros(shape=(count, 5))
    x_range_me_preflop = np.zeros(shape=(count, 6))
    x_range_me_flop = np.zeros(shape=(count, 7))
    x_range_me_turn = np.zeros(shape=(count, 7))
    x_range_me_river = np.zeros(shape=(count, 7))
    x_range_me_board = np.zeros(shape=(count, 10))

    x_range_op_single_data = np.zeros(shape=(count, 10))
    x_range_op_aggr_pass = np.zeros(shape=(count, 5))
    x_range_op_loose_tight = np.zeros(shape=(count, 5))
    x_range_op_preflop = np.zeros(shape=(count, 6))
    x_range_op_flop = np.zeros(shape=(count, 7))
    x_range_op_turn = np.zeros(shape=(count, 7))
    x_range_op_river = np.zeros(shape=(count, 7))
    x_range_op_board = np.zeros(shape=(count, 10))
    return x_single_data, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board


def fillXpost(input_raw, x_single_data, i, x_aggr_pass_me, x_loose_tight_me, x_aggr_pass_op, x_loose_tight_op, x_hole_cards, x_latest_action, x_preflop, x_flop, x_turn, x_river, x_board, x_hand_outcome, x_outs, x_range_me_single_data, x_range_me_aggr_pass, x_range_me_loose_tight, x_range_me_preflop, x_range_me_flop, x_range_me_turn, x_range_me_river, x_range_me_board, x_range_op_single_data, x_range_op_aggr_pass, x_range_op_loose_tight, x_range_op_preflop, x_range_op_flop, x_range_op_turn, x_range_op_river, x_range_op_board):
    x_single_data[i] = input_raw[:15]
    x_aggr_pass_me[i] = input_raw[15:20]
    x_loose_tight_me[i] = input_raw[20:25]
    x_aggr_pass_op[i] = input_raw[25:30]
    x_loose_tight_op[i] = input_raw[30:35]
    x_hole_cards[i] = input_raw[35:39]
    x_latest_action[i] = input_raw[39:42]
    x_preflop[i] = input_raw[42:48]
    x_flop[i] = input_raw[48:55]
    x_turn[i] = input_raw[55:62]
    x_river[i] = input_raw[62:69]
    x_board[i] = input_raw[69:79]
    x_hand_outcome[i] = input_raw[79:95]
    x_outs[i] = input_raw[95:122]

    x_range_me_single_data[i] = input_raw[122:132]
    x_range_me_aggr_pass[i] = input_raw[132:137]
    x_range_me_loose_tight[i] = input_raw[137:142]
    x_range_me_preflop[i] = input_raw[142:148]
    x_range_me_flop[i] = input_raw[148:155]
    x_range_me_turn[i] = input_raw[155:162]
    x_range_me_river[i] = input_raw[162:169]
    x_range_me_board[i] = input_raw[169:179]

    x_range_op_single_data[i] = input_raw[179:189]
    x_range_op_aggr_pass[i] = input_raw[189:194]
    x_range_op_loose_tight[i] = input_raw[194:199]
    x_range_op_preflop[i] = input_raw[199:205]
    x_range_op_flop[i] = input_raw[205:212]
    x_range_op_turn[i] = input_raw[212:219]
    x_range_op_river[i] = input_raw[219:226]
    x_range_op_board[i] = input_raw[226:]


def savePredTruth(predictions, truth, fname, out_dim):
    pred_truth = np.zeros(shape=(2*prediction_count, out_dim))
    x = 0
    y = 0
    for i in range(0, 2*prediction_count):
        if i % 2 is 0:
            pred_truth[i] = truth[x]
            x += 1
        else:
            pred_truth[i] = predictions[y]
            y += 1
    np.savetxt(pred_truth_path + fname, pred_truth.round(decimals=2), delimiter=',')


def fileLen(fname):
    with open(fname) as f:
        for i, l in enumerate(f):
            pass
    return i + 1


def vzl(model, fname):
    plot_model(model, to_file=model_path + fname, show_shapes="true")


def main():
    np.set_printoptions(formatter={'float': lambda x: "{0:0.1f}".format(x)})

    trainPreflopAction()
    trainPreflopAmount()
    trainPostflopAction()
    trainPostflopAmount()

    print("Done MF")


if __name__ == "__main__":
    main()
