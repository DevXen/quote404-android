# -*- coding: utf-8 -*-
"""Translate chunk 22, records 500-599 (IDs 404569-404668)."""
import json, re

PATH = "/home/hatch/workspace/translation-work/bashim"
SRC = f"{PATH}/chunks/chunk_22.jsonl"
OUT = f"{PATH}/out/chunk_22.jsonl"

src_lines = open(SRC, encoding="utf-8").read().splitlines()
by_id = {json.loads(l)["id"]: json.loads(l) for l in src_lines}

batch = [json.loads(l) for l in src_lines[500:600]]
assert len(batch) == 100
ids = [r["id"] for r in batch]
assert ids == list(range(404569, 404669)), (ids[0], ids[-1])

# Cyrillic <...> nick from 404647, byte-identical from source
nick647 = re.findall(r"<[^>\n]*>", by_id[404647]["text"])[1]

en = {}

en[404569] = (
'a girl\'s dating site profile says:\n'
'Looking for a man who\'ll help pay off a 180k loan. Serious relationship possible.'
)
en[404570] = (
'phone dialogue:\n'
'- where are you?\n'
'- fishing with Lyokha\n'
'- how\'s it going?\n'
'- just caught a bream.\n'
'- from Lyokha?'
)
en[404571] = (
'she: Hi )\n'
'he: hi\n'
'she: can I ask you a favor? )\n'
'he: sure\n'
'she: my brother bought a router and a wifi station, but can\'t set up the internet (hands grow out of his ass). can you help? I\'ll feed you)))\n'
'he: Can I help today?'
)
en[404572] = (
'Sword\n'
'From Medvedev\'s article\n'
'"We need the money and technologies of the countries of Europe, America, Asia."\n'
'\n'
'Sword\n'
'that\'s it!\n'
'\n'
'Sword\n'
'we need peace. preferably all of it.'
)
en[404573] = (
'Shureg: So in theory, the righteous go to heaven, and sinners to hell.\n'
'Shureg: But for dumbasses there must be a special place.\n'
'sleeping fox: an endless field covered in rakes?'
)
en[404574] = (
'yyy: A photographer friend writes\n'
'yyy: A bride brought the photos back with complaints. Says, for that kind of money he could\'ve photographed her skinnier. )'
)
en[404575] = (
'xxx:\n'
'excerpt from the dorm rules:\n'
'"... It is forbidden to grill shashlik inside the blocks on bare electric wires, or other devices, including powerful incandescent lamps...\n'
'...It is categorically forbidden to assemble jet engines or other devices from refrigerators or any other household appliances inside dorm premises..."\n'
'\n'
'xxx:\n'
'makes you proud of your home faculty )'
)
en[404576] = (
'Kashchey: they\'re going to run ground metro to the airport\n'
'evgeny: didn\'t say when?\n'
'Kashchey: your kids will ride it, don\'t worry )\n'
'evgeny: ... to visit their grandkids'
)
en[404577] = (
'xxx: The granite of science usually tastes like Doshirak noodles, less often - dumplings'
)
en[404578] = (
'xxx: the ad "We\'ll sell your apartment like it\'s ours" kind of scares me'
)
en[404579] = (
'Hersonia: a banner just popped up: "You\'ll lose 10kg easily and unnoticeably!"\n'
'Hersonia: the key word is UNNOTICEABLY\n'
'Hersonia: that\'s probably when you come to your friend and say "I lost weight", and she answers "somehow unnoticeably"'
)
en[404580] = (
'ххх: I get that everyone has their own cockroaches in the head, but this is too much. last week I asked my husband to buy buns with jam - he bought buns separately and jam separately, arguing that it\'s better that way\n'
'ууу: well maybe that\'s what the guy wanted =)\n'
'ххх: today I said - go buy soup, to quickly heat up - eat and no cooking needed, he still bought lots of soup ingredients, cooked for an hour, I nearly starved... and like that every day. I\'m sick of it. life could be so much simpler!\n'
'...\n'
'ххх: every day he keeps babbling about some gentoo... what is it?'
)
en[404581] = (
'- You, by the way I forgot, do you smoke or did you quit?\n'
'\n'
'- "Never started" sounds strange these days?'
)
en[404582] = (
'<bragin> [RST]anis: how old are you?)\n'
'<[RST]anis> me\n'
'<bragin> [RST]anis: I mean you\n'
'<[RST]anis> I\'m 25\n'
'<bragin> [RST]anis: cool, what grade are you in?\n'
'<[RST]anis> 9th'
)
en[404583] = (
'Dilvish: tell me a Russian sentence that would work as an opening to a text about education in Russia?\n'
'Lanriel: We\'re fucked, citizens!'
)
en[404584] = (
'xxx: red eyes are a sure sign of sawdust smoldering in the head.'
)
en[404585] = (
'Sharky: better to play php)\n'
'Sharky: leveling skills\n'
'Sharky: getting profession\n'
'Sharky: doing quests from management\n'
'Sharky: getting money\n'
'Sharky: buying gear with money)'
)
en[404586] = (
'From LJ:\n'
'- Ugh, ran out of thread one cross short in the embroidery!\n'
'- Take another, it\'ll be a dead pixel.'
)
en[404587] = (
'Chernomor: Girl got her license like ten days ago, afraid to drive. Decided to practice at night, driving 40 km/h. Traffic cops driving behind her. She thinks: "Well they\'re driving, I\'m not breaking anything".\n'
'Cops drove and drove, then turned on the light bar, overtook her and stopped her.\n'
'She to them: "What do you want?"\n'
'They to her: "You drive kind of strangely, miss"\n'
'Her: "Well my license is 10 days old"\n'
'Them: "That\'s all fine, of course. Only it\'s night now, your cabin light is on, and your headlights are off. Please do the opposite"'
)
en[404588] = (
'xxx: They say taxi drivers don\'t know the rules...\n'
'xxx: Was driving behind a taxi today, the driver didn\'t break the rules ONCE. Didn\'t even speed\n'
'xxx: Then didn\'t catch up with him at the traffic light\n'
'xxx: Turned out, a fully-uniformed traffic cop was sitting in the passenger seat'
)
en[404589] = (
'х - whatcha doing?\n'
'у - reading world economy news for June...\n'
'х - find me how to earn a suitcase of money))\n'
'у - sec\n'
'...\n'
'у - The Zimbabwean currency rate dropped yesterday to a record low - 1.2 billion Zimbabwean dollars per one US dollar.'
)
en[404590] = (
'Clarifying the purchase list for a tour group of eight:\n'
'\n'
'Galcka\n'
'WHY DO WE NEED 19 AXES???\n'
'\n'
'Solo\n'
'Well what if we lose 18?..\n'
'Logical.'
)
en[404591] = (
'She: tell me your fantasy? :-[\n'
'He: I want to climb onto the chariot standing on the St. Petersburg administration building xD\n'
'She: don\'t you have fantasies not only about yourself?!\n'
'He: I want you to climb onto the chariot standing on the St. Petersburg administration building...'
)
en[404592] = (
'cyberdemon: Stumped our sysadmin with the question: "What would you want more right now, a hard drive or hard sex?"\n'
'cyberdemon: He\'s been hanging for 5 minutes...'
)
en[404593] = (
'<xxx> I wonder, why does all kinds of shit happen at night?\n'
'<yyy> shit is a nocturnal animal\n'
'<yyy> by day it sleeps soundly\n'
'<yyy> and at night it comes out to feast on crooked hands'
)
en[404594] = (
'xxx: you had any problems with Philips blanks?\n'
'yyy: yeah\n'
'yyy: the bastards run out at the wrong time'
)
en[404595] = (
'Shelly: Just got a call from the office where I adminned 4 years ago, asking for the server password.\n'
'Shelly: To my reasonable suggestion that the password\'s been changed ten times by now, they answered that the "new" sysadmin hasn\'t touched anything on the server yet.'
)
en[404596] = (
'xxx: and also he, when he just started working, watered all the flowers in the department with alcohol, which our admin kept in a mineral water bottle to drink unnoticed.\n'
'yyy: and what happened to him? =)\n'
'xxx: what usually happens to dumbasses?\n'
'yyy: he got appointed head of department?\n'
'XXX: you knew.'
)
en[404597] = (
'Voffkaa:\n'
'still arbeitin\n'
'and when I finish arbeitin, gotta do our apartment repairs\n'
'already installed the door on Saturday\n'
'\n'
'Dymcheg:\n'
'you\'re a wizard, I also want a door on Saturday, better yet a door on Friday evening'
)
en[404598] = (
'from the comments to "DIY. Radio without batteries" on habr\n'
'\n'
'sse\n'
'September 20 2009, 22:31\n'
'In my opinion, Radio Mayak can be received even on a sausage with ketchup :) In a homemade receiver with lousy selectivity it\'s harder to get rid of it than to receive it.'
)
en[404599] = (
'xxx: dash\n'
'xxx: dash\n'
'xxx: dash\n'
'xxx: dash\n'
'yyy: "Dash" is spelled without the soft sign, I\'m not a verb'
)
en[404600] = (
'Adamych©: 13300 Ukraine. Zhitomir region, Berdichev, Mekhanizatorskaya **.\n'
'Casper: Is this a Moscow address?\n'
'Adamych©: Yeah, fuck, Moscow...'
)
en[404601] = (
'xxx: I\'m going to the institute dean\'s office this week\n'
'xxx: the dean will bite me there, and I\'ll become a student again =(('
)
en[404602] = (
'From a forum, discussion of nudity.\n'
'ххх: basically haven\'t tried only in front of my loved one\n'
'ууу: And in front of everyone else you have?\n'
'ххх: no, you just want to see so bad\n'
'zzz: I\'d like to see some punctuation...)'
)
en[404603] = (
'unReaL: mom brought "curiosity" candies, I\'m kind of afraid to eat them before uni :('
)
en[404604] = (
'xxx: tell me brifely what those ship battles are about\n'
'xxx: thanks in advanse\n'
'\n'
'yyy: brifely basicaly you get on a ship and shoot from canons at another ship until it explodes\n'
'\n'
'yyy: Damn, reminds me of the wonderful story "Flowers for Algernon" - "Report about what happens".'
)
en[404605] = (
'Rin: Near the metro another flyer guy was crawling around. Rambling about something, advertising a new stationery store..\n'
'Rin: Impressed by the approach. Approximate text of one gem: "You\'re young, but you already know the price of freedom? You hate being put in boxes? You want to finally be let loose? Only at our place - notebooks without margins from nine rubles fifty kopecks!"'
)
en[404606] = (
'xxx: Not only have they been replacing our elevator for two months and I\'m fed up walking to the 9th floor.\n'
'xxx: Screw it that some Tajiks are installing the elevator.\n'
'xxx: But when going down the stairs I hear: "We cannot install elevator, boss didn\'t bring SCOTCH!"...\n'
'xxx: I\'m getting really scared.'
)
en[404607] = (
'ххх: Anya, when will you start wearing women\'s clothes?\n'
'ууу: I do. A bra'
)
en[404608] = (
'xxx: The internet died at work - I think, let me go make some tea. I come out, and there are like 40 people with the same idea )\n'
'yyy: The census of idlers was a success.'
)
en[404609] = (
'Kitchen talk at work (about .NET):\n'
'- First framework, then second. Third, fourth, fifth... And then, look, retirement...'
)
en[404610] = (
'Going to the doctor for a leech session. Two girls approach me and ask: tell me, where are these bloodsuckers? I point at the doctor\'s office door. They: no, we need the bailiffs service! Turns out they\'re in the same building:-)'
)
en[404611] = (
'xxx: disappoint the girls\n'
'xxx: give a friend a Sega and a Mortal Kombat trilogy cartridge for his birthday'
)
en[404612] = (
'xxx: today in the store standing choosing spices\n'
'xxx: on the shelf lie "Kroshka-onion", "Kroshka-garlic"\n'
'xxx: where\'s "Kroshka-raccoon"?!'
)
en[404613] = (
'she: I\'m in the shower *BYE*\n'
'he: I\'m undressing and quietly entering your shower...\n'
'he: taking the gel from the shelf...\n'
'he: gently hugging you from behind and starting to soap you up...\n'
'he: rub...\n'
'he: rub...\n'
'she: false\n'
'he: damn :('
)
en[404614] = (
'xxx: Well, I always had more muscles than brains\n'
'xxx: Once after school I moonlighted as an auto mechanic. And a seasoned driver told me to bolt the front springs on a GAZ 53 - says - tighten the nuts on the ladder until it snaps.\n'
'xxx: Well, he thought he was joking, probably...'
)
en[404615] = (
'ххх(13:19:16 23/09/2009)\n'
'When I was about a year old I twitched my shoulder, mom ran with me to the neurologist, she prescribed such psychotropics that even adults get them with caution! Mom naturally didn\'t feed them to me, we called a professor from a private expensive clinic, he came, examined, then says: bring me all her things.\n'
'Mom brought them, he continues: these rompers with buttons are good, but throw out the ones with ties - you tie them unevenly and one shoulder slips off, that\'s why she twitches it. )'
)
en[404616] = (
'He: You\'ll have C:\\ written there\n'
'She: Stop mocking me! I seriously asked for help!! Why would the computer suddenly draw smileys for me?!'
)
en[404617] = (
'Rilia: In Japan there\'s a fashion for small breasts size 0-1\n'
'Flaco: That\'s not fashion, that\'s a bad harvest! xD'
)
en[404618] = (
'from a contract:\n'
'... represented by CEO Buga G.A.'
)
en[404619] = (
'xxx: has no one ever told you you have an inflated self-esteem?\n'
'ууу: You know\n'
'ууу: As one smart person said - what matters is not how a person values himself, but how others value him.\n'
'ххх: and who is this smart person?)\n'
'ууу: That\'s me'
)
en[404620] = (
'xxx: We had this guy in our (music) band. Decided to make an electric guitar himself, found some body, bought pickups, all the stuff\n'
'xxx: Crafted his miracle for a week and a half or two\n'
'xxx: Came to the next rehearsal all proud. Shows his creation - turned out nice )\n'
'xxx: Plugs the guitar into the combo, that into the mixer, volume to max, and from the speakers comes: "You\'re listening to Radio Mayak"\n'
'xxx: In short, no rehearsals for two weeks )'
)
en[404621] = (
'From an IT forum:\n'
'"If I had known that in 5 years my comp would look like it does now, I never would\'ve bought a $200 system case."'
)
en[404622] = (
'ххх: You should turn off CapsLock.. Or is it stuck?\n'
'ууу: THE CAT PRESSES IT WITH ITS PAW'
)
en[404623] = (
'Letter to support:\n'
'my profile ranks 17,833,742.\n'
'total users 17,533,750\n'
'question: Where do the hands of the Indian who wrote the statistics grow from? My self-esteem is already low. ASSHOLES!!!'
)
en[404624] = (
'R: and I\'m sitting normally in a suit, all dressed up\n'
'R: and working\n'
'F: wow\n'
'R: since Monday going to work in a suit\n'
'F: what\'s that about?\n'
'R: washing machine broke ('
)
en[404625] = (
'xxx: And Dima, in response to my statement that he\'s a deer, said that I\'m an antelope, and he\'s bending me over.'
)
en[404626] = (
'From a forum:\n'
'\n'
'kozha: Hi, I am Italian poster with Google Translator so don\'t worry about my Russian\n'
'I use this patch to dual spec, but I got an error: I train dual spec talent, I spend my talent on both Spec with same talent, I can change specs and Relog, bonus talents applied twice.\n'
'Fog: Do not use this patch, it\'s bugged. And Google translators too, please.\n'
'SeT: What else should he use? Install Promt?)\n'
'Fog: Imho better he writes in English than calls himself a poster.'
)
en[404627] = (
'Lil\'ok: A woman walks into the store, holding a dog under her arm\n'
'Lil\'ok: Goes to the bag lockers, looks around and shoves the dog in a box\n'
'Lil\'ok: Locks it and goes shopping!'
)
en[404628] = (
'ххх: The stupidest act of this beauty was conspiring the cell number listed in the contact by covering the first digits of the number with asterisks - 89.'
)
en[404629] = (
'<KennyNotDie> Here the director of the Mytishchi moldy-henna processing plant wants to play dota with us. so Rejoin please whoever can'
)
en[404630] = (
'xxx: they\'re writing to us here\n'
'xxx: "Hello. I\'d like to order a design from your company. But please, make the quality as shitty as possible. I need to brag that I drew it"'
)
en[404631] = (
'xxx: Choosing a tour. Reading hotel reviews:\n'
'xxx: "Pros: the hotel next door is even worse"'
)
en[404632] = (
'ххх: Andrey called me Olya.\n'
'ххх: Oops, I mean Gleb did.'
)
en[404633] = (
'Lord: Recently went fishing to Tver region, listen, the places are wonderful, Mother Russia, fields, meadows, edges, lakes, churches, monasteries, tiny villages, class. Stopped at a traffic light, buddy dozing, and then somewhere behind in the distance, a wild drawn-out screech of brakes, a dull thud, and in the same second the bells start ringing. Buddy through sleep: "Now that\'s service!".'
)
en[404634] = (
'xxx: Me and the cat learned a new command: when I take off my slipper, he jumps on the windowsill and shuts up.'
)
en[404635] = (
'®МаНгуСТ:\n'
'Dear users, we (the tech department workers) would much prefer the cause of the printer\'s failure to be a five-hundred ruble bill or even a five-thousand one, rather than coins with a nominal of five and two rubles =)\n'
'respectfully, tech department'
)
en[404636] = (
'kel: And hunting for fun is killing for fun!\n'
'Nikitka: And fishing - is that killing for fun? Or if the crucian doesn\'t squeal - it can be killed?\n'
'GyGy: Mushrooms, forgot mushrooms! Don\'t squeal and don\'t even twitch\n'
'macmahon: well, mushrooms have mowed down many of ours too'
)
en[404637] = (
'xxx:\n'
'And yesterday I dreamed you gave me carnations. Red ones.\n'
'xxx:\n'
'Woke up - surprised. That\'s so uncharacteristic of our relationship. If you\'d given me some piece of iron - I wouldn\'t be surprised.\n'
'yyy:\n'
'Red carnations?'
)
en[404638] = (
'xx: lying in bed, watching a movie. mechanically feeling under the pillow with my hand. suddenly felt some nasty prickly tail.\n'
'\n'
'xx: got terribly scared what the hell it was, yanked it out of the bed with all my strength.\n'
'\n'
'xx: turned out to be my own hair.'
)
en[404639] = (
'From LJ:\n'
'Thailand, tour bus. Another stop, the bus fills up, the guide says: "Look at your neighbors, is everyone here?". A drunk huge Russian guy, sprawled out, yells: "Yeees!!! Everyooone!!! Let\'s go, commander!!!!". 40 minutes later the guide gets a call from Moscow from the travel agency with wild cursing: "Have you gone totally mad????? You left two people at the stop!!!!!!". The bus turns around, comes back. Turns out they forgot to pick up the wife and mother-in-law of that very drunk guy)'
)
en[404640] = (
'xxx:\n'
'News feed for September 22:\n'
'"The Verkhovna Rada started working after three weeks of idleness".\n'
'\n'
'xxx:\n'
'A few hours later:\n'
'"Rada deputies broke the speaker\'s microphone and dispersed".\n'
'\n'
'xxx:\n'
'The guys worked fruitfully...'
)
en[404641] = (
'Friday. Evening. Office of a big IT company. Voice from the speakerphone broadcasting to the whole office:\n'
'Chief sysadmin: Dear employees! On the weekend there\'ll be a power outage, so please don\'t come to work on Sunday from 9 to 12.\n'
'a minute later the voice again:\n'
'Chief sysadmin: Small clarification - from 9 to 12 AM...'
)
en[404642] = (
'xxx: Brought the kid to school today\n'
'ххх: The teacher complains the class is very difficult, everyone at different levels, different temperaments...\n'
'ххх: Someone, she says, talks to a pencil... :)'
)
en[404643] = (
'-----autoforum ----\n'
'Driver Sep 24 2009, 00:19\n'
'Want to send my curses to the not-good person Yuri, working as a priest. Driving a VAZ-21150, he managed in a half-empty parking lot on Svobody Square to drive backwards (!!!) face-first (!!!) into my friend\'s Galant\n'
'\n'
'ENGI Sep 24 2009, 00:47\n'
'The devil must\'ve confused him.\n'
'\n'
'Driver Sep 24 2009, 00:59\n'
'Judging by how it happened, he confused the pedals.'
)
en[404644] = (
'Prosto_Lee: Will you be my tutor?\n'
'Vulpes <vugo> Goopie: Here\'s the thing, catch the thiiin line ) You can just help brush up English, but being a tutor means charging for it )\n'
'Prosto_Lee: Dumplings\n'
'Vulpes <vugo> Goopie: Agreed'
)
en[404645] = (
'ххх: I\'m du(\n'
'ххх: three years living in this apartment, well I have a neighbor Vovka. Remember?\n'
'ххх: I couldn\'t figure out why he looks at me sideways.\n'
'ххх: damn! his name\'s not Vova! It\'s Sasha! And he has a twin brother named Nikita!\n'
'ххх: so, I always saw them separately and called both of them Vova ('
)
en[404646] = (
'He: You used to be fatter.\n'
'She: Fuck, not "you used to be fatter", but "you\'ve lost so much weight"! Learn to give compliments!'
)
en[404647] = (
'<dms> I woke up at 2 PM\n'
+ nick647 + ' lucky...\n'
+ nick647 + ' me too.\n'
+ nick647 + ' but I got out of bed at 8 AM and went to work.'
)
en[404648] = (
'triklozoid: I think I\'m starting to get relationships.\n'
'triklozoid: The main thing is the girl shouldn\'t be bored.\n'
'triklozoid: Can\'t cheer her up, then at least upset her with something.'
)
en[404649] = (
'xxx: Admin slang is all bullshit. Worse is when slangs collide. I remember plunking on the piano, and a musician friend says: "You\'ve got a bug in your code here" - that\'s when my brain exploded...'
)
en[404650] = (
'ххх: Today I bought a tourniquet, gonna make a slingshot....\n'
'ууу: Why do you need it?\n'
'ххх: The deputy director banned me from shooting the air gun in the server room.'
)
en[404651] = (
'Azim: AND ARE YOU ON VKONTAKTE?????\n'
'Sasha: Nope\n'
'Azim: ON ODNOKLASSNIKI???\n'
'Sasha: No\n'
'Azim: MAN YOU\'RE WITHDRAWN'
)
en[404652] = (
'<divX> Line outage. Our specialists are fixing the fault now.\n'
'<YAS> THIRD DAY IN A ROW???!!!!\n'
'<divX> We have a very complex infrastructure.\n'
'<YAS> so complex you yourselves don\'t have a single person who understands any of it?\n'
'<divX> We do. Two. Though one is currently taking a course of forced treatment in a psychiatric hospital, the second is under investigation.'
)
en[404653] = (
'NATALI: I work at a Bank. head of retail lending\n'
'NATALI: at 22\n'
'buka104: so the boss\n'
'buka104: I can assume you have good connections, like relatives\n'
'buka104: you didn\'t come from some assholeville and suddenly become a princess like in a fairy tale\n'
'NATALI: I achieved it myself without connections!\n'
'NATALI: except that my mom works at the bank'
)
en[404654] = (
'<iGlyuck> And still, bro, Pyatigorsk is a solid city.\n'
'<iGlyuck> Here the city info line is 404-404.'
)
en[404655] = (
'жжж: I keep dreaming the same dream\n'
'жжж: Fyodor Bondarchuk approaches me\n'
'жжж: and offers me a role in his next film\n'
'жжж: and I tell him - do you think that\'ll save him?'
)
en[404656] = (
'ххх: At philosophy the lecturer told us that techies are stupid because they think all problems can be solved with science.\n'
'ууу: HERETIC!!!!!!!'
)
en[404657] = (
'Olga: me and my brother in the 90s moonlighted at a store, helping mom. my brother came up with a PR move: he stuck stickers on some products saying "made in Russia, Bliad", and then told people that Chinese manufacturers are sure Bliad is a city in Russia, not a frequently used swear word. Mugs, butter dishes and other crap sold like hotcakes, as souvenirs)'
)
en[404658] = (
'Tanya: I recently lost 10 kg of extra weight!\n'
'Svetik: how??\n'
'Tanya: my daughter learned to walk...'
)
en[404659] = (
'xxx: There\'s no such thing as internet that\'s too fast.'
)
en[404660] = (
'ууу: why didn\'t you pick Lesha up from kindergarten?!!!\n'
'ххх: couldn\'t, lots of work\n'
'ууу: when I married you, I didn\'t think work would come before family!\n'
'ххх: And when you added me on ICQ with the message "Hi. how are you? Let\'s meet!", I didn\'t think I\'d marry you'
)
en[404661] = (
'Argentum\n'
'by the way if you\'re having trouble combining C# and C++ try writing everything in C#. there are built-in classes for working with sockets, with blackjack and hookers\n'
'\n'
'Argentum\n'
'to be precise, such are the classes, not working with them :)'
)
en[404662] = (
'She: Walking home from work today, I so wanted potato pancakes! I think, I\'ll come home and make them. And when I came, I remembered I don\'t have eggs... =\\\n'
'He: (rofl) Yeah, frying pancakes is not women\'s business.'
)
en[404663] = (
'She: Do you even go to the theater?\n'
'He: Used to...\n'
'He: to New Year matinees...'
)
en[404664] = (
'<kosmik> well where are you beauties! write let\'s chat\n'
'<XV-22> kosmik: turn your head 360 degrees\n'
'<XV-22> you\'ll crack'
)
en[404665] = (
'XXX: Fuck! I\'m not flying planes anymore!\n'
'УУУ: What happened?\n'
'XXX: Was in the aviation school dorm.\n'
'УУУ: ))))\n'
'XXX: Laugh, I laughed too when I saw the rastaman dispatcher.\n'
'XXX: But after the emo pilot, I got scared.'
)
en[404666] = (
'She: O holy prostate!\n'
'He: Uh... either it\'s ignorance of grammar and punctuation, or - the birth of a new religious cult!'
)
en[404667] = (
'*Preparation for the speech culture seminar*\n'
'\n'
'ххх> Sanya, give an example when a wrongly chosen synonym can badly distort a phrase\'s meaning?\n'
'A.Z.> uhhhm...\n'
'A.Z.> "powder one\'s nose" and "powder one\'s nostrils" - feel the difference? ;-)\n'
'ххх> hehehe)'
)
en[404668] = (
'sl33py: administrative expenses - are those expenses for admins\' beer?'
)

assert set(en.keys()) == set(ids), (set(ids) - set(en.keys()))

# Newline parity check before writing anything
bad = []
for r in batch:
    ru = r["text"]
    e = en[r["id"]]
    if ru.count("\n") != e.count("\n"):
        bad.append((r["id"], ru.count("\n"), e.count("\n")))
if bad:
    raise SystemExit(f"NEWLINE MISMATCH: {bad}")

# <...> token preservation check
bad = []
for r in batch:
    toks = re.findall(r"<[^>\n]*>", r["text"])
    for t in set(toks):
        if t not in en[r["id"]]:
            bad.append((r["id"], t))
if bad:
    raise SystemExit(f"NICK MISMATCH: {bad}")

with open(OUT, "a", encoding="utf-8") as f:
    for r in batch:
        rec = {"id": r["id"], "ru": r["text"], "en": en[r["id"]]}
        f.write(json.dumps(rec, ensure_ascii=False) + "\n")

n = sum(1 for _ in open(OUT, encoding="utf-8"))
print(f"appended 100, out now has {n} lines")
