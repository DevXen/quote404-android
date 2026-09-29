#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 2b (IDs 438297-438336)."""
import json

EN = {
438297: """xxx: You come to work, and some guy had an increment
xxx: And he treats everyone to pies for it
yyy: Just thought: it'd be cool if every variable treated everyone for its increment.""",
438298: """As experience shows, the phrase "My battery's almost dead, let's be quick" at the start of a call doesn't affect negotiation effectiveness at all, but shortens them about threefold.""",
438299: """xxx: In the best traditions of mechanical engineering - all the mechanism's parts that fail most often are located in the most inaccessible place.""",
438300: """Got an SMS from the internet provider (spelling preserved):
"Dear Client, now you can speed up your home internet even more profitably!"

My brain smoothly switches the inner reader's voice to Jamshut's voice as I read...""",
438301: """Wife: How are you?
Me: I'm kinda already beat, and the horse hasn't even rolled yet.
Me: Hmm, turned out to be a sketch from a zoophile's life...""",
438302: """A compliment from a techie: "Your photo IMGP5119 is very cute ^^ \"""",
438303: """xxx: no no, everything's fine
yyy: in general, when a girl says everything's fine, you need to RUN. RUN LIKE YOU'VE NEVER RUN, CARL!
ххх: at least you get it
ууу: I dated a Feo. believe me, from the written phrase "everything's fine" I can determine her intonation and set the right speed and direction of running.""",
438304: """Ххх: The grass used to be greener, and the Internet more interesting.

Ууу: Tell me about it, lately the Internet got so boring I have to read books on my specialty and write articles just to entertain myself somehow.""",
438305: """xxx: The little kid loves cartoons. Asks me a question: why are the Barboskin puppies different breeds?
xxx: Was stumped for an answer.""",
438306: """Discussions from the find-the-place-by-photo forum:
xxx: Brussels, Belgium
yyy: How?
xxx: Well you know it was all very simple. First I analyzed the primary factors. A battered "Beetle", Belgian plates means it doesn't go to any exhibitions, consequently we rule out the Netherlands, Holland and the like. Then came the secondary factors, the extremely characteristic foliage, masonry and the missing pole — analyzing them I noticed that the post author on the toska subsite, the esteemed rothbard, replied to my inbox and told me where he took this shot""",
438307: """It's kinda stressing me out, our elderly boss lady who every morning calls a same-aged friend on the internal phone and starts the conversation with: 'Oh, you're still alive!? well then hello!!'""",
438308: """To the question of at what speed light propagates in fiber optics, an acquaintance, a medic-physicist, began his reasoning like this: "in one nanosecond light travels the length of an average penis...\"""",
438309: """xxx: How to lure a pug out of hiding.
xxx: Step one - open the fridge.
xxx: That's it.""",
438310: """In general, there are prudent client representatives who know exactly what a raised floor is for. Down there in their server rooms lies vodka, chilling. At one ventilation manufacturer we saw a small server room with a properly equipped hot exhaust. They decided not only to heat the environment but to properly use the heat for their own benefit. Installed a small brewing unit, rejoicing in their homebrew.""",
438311: """Working with a friend as installers at a construction site. Elite building, we go to the top floor, it's like a penthouse: only two apartments per floor and each is 5-7 rooms plus a terrace..
Walking down the corridor, I hear my partner humming something, listened closer: iiiit's cooommunal, communal apartment, iiiit's cooommunal, communal country.""",
438312: """"Microsoft will release SQL Server for Linux in 2017"

<dsmirnov> soon microsoft will announce windows for linux""",
438313: """<dsmirnov> golden hands from the ass: fixed the laptop myself, dropped""",
438314: """xxx: just now walking up to the house - a crow sitting in a tree meowing hoarsely
xxx: teasing the neighbor's cat
xxx: ...the first day of spring was underway :D""",
438315: """xxx: Today I chopped a pumpkin with a machete. The day wasn't lived in vain.""",
438316: """xxx: No, we figured out recently, you can just sprinkle baking soda on the Alien to neutralize its acid.
yyy: Ok. And what do we sprinkle its teeth, claws and sharp tail with?)
zzz: Persimmon. To make it astringent.""",
438317: """For the 23rd our girls gave each boy a bottle of toilet water. Good stuff, I must say. And today there was a bonus: three socks for each boy! "We don't think you're mutants, it's a spare\"""",
438318: """About kindness, care and tact. Once we went with friends to the dacha. In their "Tavria". Everything was fine, except the Tavria had something with a wheel, maybe a bulge, or maybe just unbalanced. The car shook in the front so much that on the way back I got blisters on my legs. Blisters, Carl! Sitting in a car! And finally, the long-awaited city. I tactfully ask to be dropped off at the nearest metro.. Anywhere already! And they offer to drive me home. I don't even know what exactly gave me the strength not to scream "NOOO!!!"..""",
438319: """The joke about toys nailed to the floor is as old as the world
My daughter today learned the modern version from her own experience: sandals stuck to the carpet by their velcro to the death)))""",
438320: """xxx: in St. Petersburg at the site we needed to get 5x95 cable about 6 meters. And they dragged in a hundred-meter tonnage reel. They say: "Might come in handy. If not — well, we'll take the scraps.\"""",
438321: """CEO (advertising agency) about the banner design, looking at the mockup:
Director: You forgot to draw a bow on the bee with the frog face here
Manager: That's a boy bee with teeth like a bunny's
Me: somewhere in this whole story I can feel the illogicality right in my ass...""",
438322: """— Did anyone ask you?
— Me? I'm a columnist. You don't even need to ask me, I'll tell you everything unprompted""",
438323: """Discussing the new Bugatti

xxx: Will everything be OK with CO2?
yyy: for 2.4 mil you also buy a CO2 emission license ;)
zzz: And the unused quota can be sold to Japan...
aaa: and get a Prius as change""",
438324: """xxx: Acquaintances' kids play "lego", and the right side of the street is guarded by law-enforcement agencies, and the left by left-enforcement agencies. Still can't decide which one I'd like to live on.""",
438325: """xxx: One of my colleagues recently secretly brought a hedgehog to work, because at home it kept him from sleeping at night. After a while the hedgehog ran away. It was fun watching security set up ambushes at night, since the motion sensor in the server room kept going off. The poor guys couldn't sleep, thinking intruders were getting into the server room. And when the hedgehog was caught, I learned how to make four mistakes in a two-letter word. This weirdo in his explanatory note wrote the word hedgehog as "йошь".""",
438326: """xxx: After another morning argument about who gets the bathroom first, my wife delivered: "You look like Cupid, but act like Timur!\"""",
438327: """Karina: A programmer needs algebra

Theli: not necessarily ) depends on what you'll be programming
or rather, in what language

Karina: In Russian probably

Theli: got it )""",
438328: """Came back from sick leave for the first day, complaining to my husband:
Me: The crocodile won't catch, the coconut won't grow :(
H: You mean, can't get into work?
Me: Yeeah :(
H: Well, you need to do everything gradually - first just sit. The first week. Then - you can turn on the computer. Gradually...""",
438329: """Biblusha: Denial, Anger, Bargaining, Depression, Acceptance
Muxto: Saturday, Sunday""",
438330: """From FB
X > Sensation! The real Linux symbol found! It's a penguin figurine assembled from a Lego set (photo).
Y > The real one should be from constructor pieces printed on a 3D printer.
Z > Hipsterism! The Linux way is a hacksaw and a file!""",
438331: """A year ago we neutered the cat. In summer we went to the dacha in the village. Let the cat out for a walk as usual. Gone for two days! Then came back all dirty, in the company of two lady cats. White and ginger, quite young. And so they lived with us on the plot half the summer. In short, it's not the balls that make the man, it's CHARISMA...""",
438332: """xxx: And still the family kitchen would stop and collapse without me: no matter how the wife spins, it's me who tastes for salt.""",
438333: """Loy Yver: I have willpower-cured arachnophobia. I'm not afraid of spiders now, but they still cause a feeling of apprehension in me. I wouldn't move out, I'd try to catch the spider and evict it from the apartment.

whc: With its things?

Loy Yver: Yes! Let it roll up its web and scram!""",
438334: """From a discussion of the new office

xxx: economy-class windowsill))
yyy: and curtains?
yyy: blinds?
xxx: when we asked for them, they said - what for? there's never any sun here""",
438335: """xxx: My mother-in-law got a March 8th greeting card from a former suitor. my husband's mom is, mind you, 79 years old! Oh, she said, I thought he was already dead, and I didn't congratulate him on February 23rd, how awkward.""",
438336: """Fixing a report in Excel. The formula keeps returning a wrong result. I notice some rows are hidden. I open them - they're filled black with black text. The accountant decided that since a human can't see the hidden rows written in black on black, Excel surely won't notice either.""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
