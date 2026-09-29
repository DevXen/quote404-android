"""Chunk 28 batch 7: records 601-700 (ids 410679-410778). Appends to out/chunk_28.jsonl."""
import json, os

EN = {
410679: """wlasser:
went to xml.yandex. there as usage examples there's a game: Patronymic.
The point is simple: you enter a first and last name, and yandex picks a patronymic based on search results.
So first thing I entered myself (but I'm not famous so yandex couldn't give my patronymic), then entered Vladimir Zhirinovsky, after which I beheld the expected: Volfovich.
Then entered Steve Jobs...
Runet users believe that Steve Jobs's patronymic is fucking.""",
410680: """ххх. What computers can you talk about with him, if he thought internet speed depends on RAM...""",
410681: """ххх: And imagine, somewhere in another galaxy, on planet X, in city X, house X, sits a creature with an ass instead of a head (that's how their evolution turned out) watching a video about galaxies and thinking "Probably somewhere some advanced creature is sitting in another galaxy right now pondering the existence of other galaxies and the creatures inhabiting them"

ууу: As if we don't have enough creatures with an ass&#65279; instead of a head!""",
410682: """xxx: do you think if I beg my boyfriend for beer money through sex, does that count as prostitution?""",
410683: """xxx: Once, walking with a friend, decided to stop by a store. On the doors are icons (crossed-out bottle, ice cream, dog). And my friend, with great desire to tease me, arrogantly utters the phrase:"They won't let you in there... (silence)... Dogs aren't allowed in there!!!\"""",
410684: """In 2011 the Fantastic Four turns 50. Marvel decided to celebrate this anniversary in the most radical and shocking way: a month ago they killed one of the four heroes.""",
410685: """F078: everything has its pluses
F078: my dacha burned down
F078: outside the city
F078: now I have a cellar of baked potatoes and 4 boxes of boiled condensed milk :-)""",
410686: """from a car forum:
xxx: tell me, why when I press the clutch to shift gears, does my car sharply lose speed and nose-dive???
yyy: brother, which foot do you press the clutch with?
xxx: right. Why?
yyy: oh nothing. did you buy your license long ago? maybe it just expired, so the car refuses to drive with an expired driver behind the wheel.""",
410687: """<SAPRO> airport. entry by cards that count parking time. separate turnstile for buses. they have unlimited proximity.
<SAPRO> a double-decker bus pulls up. and then.. NANOTECHNOLOGIES!!
<SAPRO> the driver's got a flyswatter, a card taped to it with scotch tape, the driver opens the window, reaches down, applies the flyswatter to the turnstile, the barrier opens..""",
410688: """Ххх: Doing a metrology lab. The calculated relative error of the device was 23490%!
Ууу: measured current with a ruler?""",
410689: """xxx: I didn't hide porn on my disk as carefully as I hide My Little Pony episodes...""",
410690: """xxx: who rises early, to him God gives a hammer drill""",
410691: """ххх: You know, I'm sitting in front of the TV today, and there's an ad, something like "a kitten is like a baby", I turn to my cat with a smile, and he's shitting on the rug with a happy face.""",
410692: """`ExTRo`: played Assassin's Creed all day today, don't want anything, want to jump on houses and kill people. but the problem is that if I start jumping on houses, I'll kill only one person""",
410693: """Topic "Looking for baseball bats"
ххх: Where do they sell them in Kurgan? Would like aluminum and small.
ууу: Free advice. Buy a birch rolling pin at the hardware store. Traffic cops have no complaints, and it works no worse, if not better (confuses the punks) than a bat.""",
410694: """Polkovnik: Just now I walk into the store all brutal, in a biker jacket, in boots, face expression not the kindest, approach the vodka department. And there's a merchandiser boy rearranging bottles by feng shui. And he blocked, you know, exactly what I need ) I patted him on the shoulder - move over. And this miracle raises his clear gaze at me, changes in face and says: Do you need my clothes?
I was even lost for what to answer.""",
410695: """[21:59:22] * Разбудите_Витальсона_в_17_00 changes nick to Негатив
[22:01:31] <Негатив> is displeased with you""",
410696: """Kljaksa: Decided to lose weight starting today. Walked to and from work, legs hurt like hell! But saved 72 rubles on transport this way!

Kljaksa: on the way back near home couldn't resist and bought 2 chocolate bars with the saved money (""",
410697: """xxx: Today my daughter told me about a foreigner's reaction to our cuisine.
xxx: Turns out, shuba is a borscht cake! ))""",
410698: """13:55:03: we launched a sandblasting chamber in our shop, and one of the workers servicing it got drunk the same day and fell asleep right there, hugging this sandblaster. Today he brought an explanatory note. "I, so-and-so, on such-and-such date christened the launch of the sandblasting chamber for its good work." At the morning briefing they read it, were moved and decided not to punish him :-D""",
410699: """<BILBO> Came to the office first - like I wasn't even late...""",
410700: """xxx: This brainless creature declared that I can't argue! Of course, for me an argument is a reasoned defense of my position, and for her it's a ritual consisting of yelling incoherent phrases which the opponent must obediently agree with, admit his guilt and admire Her boundless wisdom.""",
410701: """xxx: Wife bought a swimming cap, spent money. And I say - why? There are five gas masks lying at home - cut along the contour - and fine, everyone will see the officer's wife came, right?
xxx: Hasn't talked to me for two hours already...""",
410702: """xxx: For once decided to wash all the dishes and they turned off the water!
УУУ: You didn't get sick, did you, what made you decide to wash dishes?
ххх: Well they turned off the internet...
УУУ: and how are you writing to me without internet?
ххх: I already ran and paid, screw these dishes.""",
410703: """xxx: posted my resume.
xxx: the first call was from my work, the one I'm quitting, with an offer to get a job with them...""",
410704: """Эльга: Why don't clinic doctors prescribe hugs? They help me better than pills sometimes...
Loki: So patients don't self-medicate and switch to stronger stuff.""",
410705: """xxx: How to drive a lover crazy?
xxx: Put on a bra with a front clasp, a body-T-shirt with a hidden clasp between the legs and stretch pants with a fake fly.""",
410706: """ihaveabomb: Home alone? Time to watch porn WITH SOUND!""",
410707: """from a car forum, discussing the unfastened seatbelt alarm

xxx: please tell me should an unfastened passenger beep??
yyy: Actually shouldn't, but if he wants to - why not?
zzz: my wife beeps when I'm not fastened.""",
410708: """Базилио: Our admin has one of the servers (a one-unit rackmount) without a rack. It didn't fit anywhere, and they haven't bought another rack yet. Now picture this: this server stands in the corner on a table, under it 4 stacks of dominoes serving as "legs", so there's space between the table and the server. The case is open, and right on top of the server stands a kitchen hood and works (extracts air). This way the overclocked xeon works within normal temperature.
The foreigners (came to the company) had their eyes so round we won't forget. They say there was even someone related to Intel there. Probably the one whose eyes were the roundest.""",
410709: """xxx: once at home there was a performance based on "The Adventures of Buratino". Drunk father climbed under the carpet on the wall and spent a long time looking for the bathroom door there :)""",
410710: """<xxx> Went to football, after read on a friendly site: "18:53 Fans in the stands are actively discussing the referee's decision, coming to the conclusion that there probably was no penalty." Is the thunderous "Referee is a faggot" a discussion?""",
410711: """pom: Until Petrovich came to work sober, everyone thought he had a lisp or some other speech defect...""",
410712: """From habr:

"In Sweden a website of the 'Church of Kopimism' started working. According to them, 'Religion is based on faith and rituals. We believe that information should be freely distributed and perform acts of copying and distribution'."

kekekeks: So now if copyright holders jail someone, can we scream about religious persecution?""",
410713: """Looking into a newspaper kiosk window:
Я: give me the "Makler" newspaper (a thick ad paper)
Продавец (П): No Makler, there's "Avizo" (Makler analog), which section do you want?
Я: is it as thick?
П: sorry, which section are you interested in?
Я: I just need to wash windows...""",
410714: """xxx: What do your coders write in?
yyy: Judging by the final product - haloperidol""",
410715: """s4z: Today the cat slept with me for the first time. I thought it got kinder and understood how I love and care for it...
s4z: Turns out they turned off the heating :(""",
410716: """xxxx: Recently read about an animal that never sleeps
хххх: If this species ever becomes intelligent, its evolution will stop at the stage of inventing online games.""",
410717: """<Chegee> What's the omen when you spill coffee?
<Печенько> means your hands grow out of your ass""",
410718: """raise request from one of the girls:

Please raise my salary by 10 thousand rubles. Since rent went up to 22 thousand rubles and I don't have a husband or his replacement.

director's resolution:

Urgently find a husband or his replacement""",
410719: """xxx: How can you cheat a urine test?
yyy: Submit tap water mixed with yellow gouache.
aaa: With the same success, you can just submit juice mixed with tap water.
zzz: Medical commission conclusion:
"Ivan Sidorov cannot serve because he is an orange.""",
410720: """Фея: you upset me
sir.Cer: And now there are three of you?""",
410721: """ххх - It's Palm Sunday, and in our entrance someone threw out a Christmas tree today...
ууу - probably only one vase, nowhere to put the willow :(""",
410722: """xxx: here's a question for you
xxx: look: in the west atheists gather in societies, hang ads on the streets, and here?
yyy: here the church handles atheism advertising, and hardly anyone could do it better""",
410723: """^^ФоксИ^^: Actually, I'm trying to get rid of all the harmful phone functions and leave one main one. But so far, as you can see, not working out well
alogicman: which one?
^^ФоксИ^^: Call receive - transmit
alogicman: you need a Nokia 3310
^^ФоксИ^^: Does it have only that function?
alogicman: no, you can also play football on it""",
410724: """xxx: let's take Shura to the cafe with us?
yyy: uhhh, let's?
xxx: is that a question?
yyy: it's a question-agreement. implies the expression "sure, but why the fuck?\"""",
410725: """Daughter didn't get into kindergarten at 3. In the queue - 108.
They take 30 kids a year, there's a chance in 3 years, i.e. at 6.
But of the 30 they take 20 priority-enrollment kids. The chance will be in 10 years, i.e. at 13.
And besides, they only take three-year-olds, the rest will be taken when someone drops out and a place frees up. She'll go at retirement age.""",
410726: """ю.г.: how to make a photo in this style?
с.ш.: with hands
ю.г.: more precisely?
с.ш.: 2 sprouts grow from the shoulders...""",
410727: """xxx: What's on a drunk's mind is in a sober person's explanatory note the next morning after the corporate party...""",
410728: """xxx: how sclerosis sometimes delights!
xxx: went to make tea, cut myself some pie in parallel. While the tea brews, carried the pie to the room and went to wait for the tea to brew, played with the dog in parallel, flipped channels.
xxx: Finally took the tea and came to the room, AND THERE'S PIE!!!!!
xxx: The first split seconds while there was the thought "where did it come from???" - it was indescribable happiness...""",
410729: """Poisonblack: Suddenly. In my status it's not beer but coffee finally.
Poisonblack: Hi, liver. How's your Dasha?""",
410730: """Stepped out to the next office, hear - they're calling. The office-manager girl opens.

Я: who was it?
Д: it's the Inquisition for Masha

I think, Masha is of course a witch, but still in broad daylight...
Я: maybe it was the cash-in-transit?
Д: oh right, I always mix them up""",
410731: """Axilles 182=): my office has a chief accountant working part-time as a cleaning lady
Axilles 182=): by day she's a regular clerk
Axilles 182=): and by night - a ruthless fighter for cleanliness and order!!!""",
410732: """From a series discussion:
aaa: You wait a month - a month and a half, and watching fits into 40 minutes. Doesn't it seem to you that someone in this Universe is seriously scamming us all?
bbb: Tried cooking food? Exactly the same crap""",
410733: """Фисташка: My son once drowned three mobile phones in tea
Kuskus: And I drowned a TV remote, a laptop, two mobiles, two table lamps
Vasyatka: Kuskus, sorry, what do you drink tea from? basins?""",
410734: """xxx: Waiting for the teacher for a psychology retake. Didn't sleep at night, eyes closing, and it's dark near the classroom. To make it clearer - several offices are combined into a separate wing there. The teacher is teaching another group, and I'm sitting under the door, zoning out in the dark. And a quiet insinuating voice from nowhere tells about symptoms of schizophrenia... Scary...""",
410735: """complaining to my husband that my son bought not what I asked.

я: he bought, attention, two, ONLY TWO chicken legs!
я: so what do I do now? how to divide two legs among five people?
муж: alright, moving on... tell me what to buy? )
муж: let me buy a chicken without legs, we already have legs )
муж: we'll put them together in the pan - it'll be like a whole one
муж: look are they different there, or two left ones, or right ones?""",
410736: """SvetlanKO: my thesis advisor is already haunting my dreams (
SvetlanKO: oh, by the way, do you have her photo? need to put it on my desktop to remind me
pRosto_Я: catch )
SvetlanKO: damn, she really does have such a reproachful face...
SvetlanKO: "Your thesis advisor is upset by your thesis" )
pRosto_Я: exactly )
- "The thesis advisor looks at you like a repeater"
- "Write the thesis. Write the thesis, bitch!"
- "She's a doctor of sciences. And what have you achieved?"
- "This is the thesis advisor. And you're pathetic shit!"
- "Ibnwrote\"""",
410737: """From Q&A. Is it true that penguins fall on their backs from flying helicopters and how does it happen?
Answer: "When a penguin at the north pole falls, it lands on the earth's axis and with its weight pushes it through a bit. Because of this at the south pole the earth's axis sticks out a bit. This is seen by the fallen penguin's relatives living at the south pole. Discovering the disorder, they push the earth's axis back. The axis pokes the fallen penguin in the back and puts it on its feet."
Guys, are you all ok?""",
410738: """xxx:
I crack up at these lovers of elegant literature. Such a guy cruises in a narrowed fitted suit, with a scarf, a flower in his "buttonhole" and expounds that in English there's no swearing, only strong language and that cocksucker is not swearing at all. The funniest thing happened when, waving his rakes he dropped a flower pot on his foot. What he said then was extremely far from "the violet's crimsoned petal". I haven't heard such swearing even from my warrant officer.""",
410739: """antarova: strip-plastic is yeees
antarova: I walk into the server room like this
antarova: and start sloooowly dancing near the switches
няко: and the network comes up by itself""",
410740: """ххх: I once walked out of a job interview in the middle of a question saying "you work here yourselves"
ууу: Well you're a handsome devil, what can I say
ххх: but you know, they called me back and hired me""",
410741: """xxx: drove a car by power of attorney, the owner died, what to do?
ууу: died long ago?
xxx: a week ago, does it matter? what to do?
ууу: bury him""",
410742: """SPARROW: Heard of a salad called ANYTHING?
SPARROW: Take a big pot, put in a handful of GO LOOK YOURSELF WHAT'S IN THE FRIDGE or MAYBE BUY SOMETHING, then boil eggs and crumble them in finely, salt and dress with mayo!
SPARROW: And to make it tasty, add the secret ingredient!
Сильфиль: Which one?
SPARROW: The secret ingredient is A WEEK OF HUNGER""",
410743: """Дмитрий: Sanya, take a bouquet of 20 roses, come to her in the morning and say: I can't live without you, love you madly. If she throws herself at you and kisses you then it's fate, love and be happy, if she starts asking questions, or just takes them like thanks - take them and leave, and don't come back.
Александр: does it have to be exactly an even number of roses?""",
410744: """xxx:
How I'm sick of the question at interviews:
- Where do you see yourself in a few years?
I just want to answer:
- As the director of your company. Right?""",
410745: """A colleague talking on the phone:
- There's shit in the class, look!
...
- Damn, what do you mean "who shits"? I'm saying, in the registry, look in CLASSES_ROOT!""",
410746: """ххх: Telling a girl I brought home, who started squeezing my kitty, "Why are you all pestering my cat!" seems to have been unnecessary...""",
410747: """ххх. And they also mocked Hitler well in that... what's it called... come on tell me...
ууу. In '45?""",
410748: """xxx: Happiness is when you wake up and get enough sleep at the same time""",
410749: """ххх: Yesterday I talked to a girl for 5 hours
ххх: more precisely she talked to me""",
410750: """xxx: my son wrote an essay at school on "my family".
xxx: I'm reading. neat childish handwriting, and such a good start: "my mom is an economist. I'm proud of her. she always tells our cat "eat your porridge for twenty-two fifty"."
xxx: how do I go to the parent meeting now?...""",
410751: """xxx: I had no idea about the relationship until I found out it ended""",
410752: """111: So what do we put in question 9?
222: Let's do whatever Ira says...
222: Iraaa, quickly, without thinking, name a number from one to three!
333: Seven!
111: Well genius, put seven ))""",
410753: """ххх:
I work in the USA. Due to well-known events, my wife and I had to look for other jobs, in other states. Found in "Fallout battle glory places": I'm in the west, she's in the east. We rarely see each other - ground transport takes too long, and after the new rules were introduced flying is not appealing at all.
The boss (an American) called me in and says:
- Come on, go, deliver this package, they won't send it by airmail anyway, and see your wife, you probably forgot what she looks like.
- Ah, so that's why you Yankees keep photos on your desks...
The boss laughed, threw in a couple more days.""",
410754: """spurs: geofaculty, scientific conference among students
spurs: a girl presents her report topic - "Influence of volcanic eruptions on the environment on the example of Eyjafjallajökull".
spurs: she'd only managed to pronounce the volcano's name (clearly and quickly) when the whole hall already burst into ovations.""",
410755: """<xxx> damn, a girl-HR just sent my colleague the attestation results. In the plan of what needs improvement, it says: "Improve inglish\".""",
410756: """<xxx> hmm, the cat jumped on the keyboard
<xxx> in Word it happily typed "xscfio"
<xxx> which is my email password
*xxx pondered the reliability of his password""",
410757: """ххх: this one ad confuses me "chicken, onion and bacon - like rock, paper, scissors, like a mustache, jackboot and rapier"

yyy: nah, it's fine... I walk to work past the back of McDonald's every day, where their kitchen exhaust is. It smells exactly like grilled jackboots.""",
410758: """xxx: Well yeah, I'd invite you for tea tomorrow, but my sister's home""",
410759: """ХХХ: I'll go take sunflower seeds from my subordinate
ХХХ: After all, I'm above him in the food chain""",
410760: """xxx: why'd they cut the water?
yyy: Куй knows...
xxx: Куй knows everything!
yyy: Oh don't, I have trauma
yyy: I once climbed into his pants saying "hello, wise Kaa\"...""",
410761: """QII:
The last few days I've been methodically inventing things to do. For example, drew performance graphs for the last three years, collected statistics for the current year... Basically, was dicking around.
And today I started washing windows at work.
QII:
The main thing, I washed two windows, against the background of my labor therapy people started coming up with other work (we have a subbotnik). I played enough with windows, washed my hands, put cream on, exhaled. And everyone's in the middle of cleaning. And then the general walks in! And I, as usual, am the only one doing fuck-all!""",
410762: """Subject - urinotherapy

ххх: Nothing funny - urine generally has a wide spectrum of possibilities. Donkey urine, as is known, increases the octane number.""",
410763: """dimassik: I want summer so bad I'm climbing the walls!
dimassik: Just imagine, you're sitting, sun, storm clouds approaching...
dimassik: Kraaaaaak!!!!
ynestulya: what's that?
dimassik: that's how a summer thunderstorm starts.
ynestulya: bullshit, a summer thunderstorm starts with "Fucking shit!!! Take the laundry in!!!" And everyone rushes outside with basins.""",
410764: """Ххх: Andryukha, can I borrow your old laptop for a month?!!
Ууу: No problem, even for life.
Ххх: Damn, thanks, friend!
Ууу: Just give it back later.""",
410765: """From LJ:
spions: There's no word "I can't". There's the word "I'm a lazy beast".
ynestulya: Touch your tailbone with your knee!""",
410766: """xxx: For my anniversary my wife gave me a car mechanic tool set, colleagues chipped in for a set of winter tires, my son bought a navigator, my daughter - a TV for the car.
yyy: Do you think it was your birthday after all, or the car's?""",
410767: """Local network chat:
xxx: don't get calluses
yyy: WHAT?
xxx: either move away from the window, or close the curtains
xxx: got binoculars as a gift
xxx: trying them out""",
410768: """ххх:
Fuck, I'm amazed. Just now a "technical director" whined to me that he can't return our company's 50 thousand rubles debt, because he has no money. Like, he didn't even have enough for a platinum edition Cadillac Escalade for himself and his wife. Had to take a simpler version for himself, "like for losers\".""",
410769: """A call.
- Hello! We bought a lot of stuff from you at the end of March, or beginning of April, and now we want to exchange some of it for something else good...""",
410770: """xxx: Probably an incoming letter from the CEO sized 28 MB with the subject "Have a nice weekend" doesn't bode me anything good %\\""",
410771: """xxx: Before, when I studied at school, we always bullied the cleaning lady, took her rag away, hid her mop with bucket, it was so fun.
xxx: Now I work part-time at school as a cleaning lady myself... not funny.""",
410772: """Mother-in-law calls from the dacha, says there's no electricity, but the neighbors have it. I advise going to the garage and checking the "breakers".
- Bottom row, second from left... Is it on?
- No.
- Turn it on.
- Won't I get shocked?
- No, mom, you won't.
- Turning it on....
And short beeps. Tried to call back for 40 minutes. Already was about to tear off to the dacha. She called back herself. Turns out the phone died, and she forgot to put it on charge right away.""",
410773: """Serezhka: military enlistment office! understand, I'm hard to find, easy to lose and impossible to forget :D""",
410774: """ххх: And at your work is tea/coffee shared?
ууу: Of course! Someone buys, someone drinks...""",
410775: """xxx: I regularly come across auto mechanics who consider it their duty to trash all other auto mechanics who repaired my car before them.
xxx: the circle has closed more than once.""",
410776: """arbooz: were you at the interview?
Liana: was..
arbooz: what did they say?
Liana: you're not suitable for us!
Liana: they asked me how much 11 percent of 200 is, I can't count that in my head:((
arbooz: fuck...
Liana: ???
arbooz: bastards, I say. they might as well have asked how much 10% of a thousand is""",
410777: """XXX: When I was little and wanted a dog - they gave me an encyclopedia about dogs. When I grew a bit and wanted a computer - they gave me an informatics encyclopedia. Now I'm in the prime of youth, want a girl. I have a bad feeling....""",
410778: """Zlobny: in Santa Barbara Cruz was the only policeman in town: he was assigned to investigate all crimes. He didn't solve a single one that I remember )""",
}

def main():
    src = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = {}
    with open(src, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000
    for i in EN:
        assert i in recs, i
    cur = 0
    have = set()
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line:
                    cur += 1
                    have.add(json.loads(line)['id'])
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            if i in have:
                continue
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_7 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
